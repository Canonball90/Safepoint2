package safepoint.two.module.movement;

import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.EventMove;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.event.events.RenderModelEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.ColorSetting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.mixin.mixins.AccessorCPacketPlayer;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.crystal.BlockPosWithFacing;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.world.BlockUtil;
import safepoint.two.utils.world.RotationUtil;
import safepoint.two.utils.world.ScaffoldBlock;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Scaffold", description = "", category = Module.Category.Movement)
public class Scaffold extends Module {

    BooleanSetting rotate = new BooleanSetting("Rotate", true, this);
    BooleanSetting airCheck = new BooleanSetting("AirCheck", true, this,v -> rotate.getValue());
    BooleanSetting render = new BooleanSetting("Render", true, this);
    BooleanSetting swing = new BooleanSetting("Swing", true, this);
    BooleanSetting Switch = new BooleanSetting("Switch", true, this);
    BooleanSetting sneak = new BooleanSetting("Sneak", true, this);
    BooleanSetting Tower = new BooleanSetting("Tower", true, this);
    BooleanSetting timed = new BooleanSetting("Timed", true, this,v -> Tower.getValue());
    DoubleSetting speedT = new DoubleSetting("Speed", 5.0, 2.0, 10.0, this,v -> timed.getValue());
    BooleanSetting center = new BooleanSetting("Center", true, this);
    DoubleSetting speed = new DoubleSetting("Speed",  0.7, 0.0, 1.0, this);
    DoubleSetting upSpeed = new DoubleSetting("upSpeed", 0.41999998688697815D, 0.0, 1.0, this);
    ColorSetting color = new ColorSetting("Color", new Color(45,244,35), this);


    private List<ScaffoldBlock> blocksToRender = new ArrayList<ScaffoldBlock>();
    private boolean teleported;
    private BlockPos pos;
    private boolean packet = false;
    Timer time = new Timer();
    private final Timer towerTimer = new Timer();
    transient private static boolean rotating = false;
    transient public static float yaw;
    transient public static float pitch;
    transient public static float renderPitch;
    transient public static boolean shouldSpoofPacket;
    BlockPosWithFacing posll = new BlockPosWithFacing(pos, EnumFacing.UP);

    @SubscribeEvent
    public void onMove(EventMove event) {
        if(mc.player == null || mc.world == null) return;
        this.pos = new BlockPos(this.mc.player.posX, this.mc.player.posY - 1.0, this.mc.player.posZ);
        if(isAir(this.pos)){
            mc.player.setSneaking(true);
        }else{
            mc.player.setSneaking(false);
        }
        if(rotate.getValue() && this.isAir(pos)){
            lookAtPos(new ScaffoldBlock(BlockUtil.posToVec3d(this.pos)), EnumFacing.UP);
        }
        if (this.isAir(this.pos)) {//ToDo add , this.mc.player.isSneaking() later
            if(sneak.getValue()){

            }
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, rotate.getValue(), this.packet);
            this.blocksToRender.add(new ScaffoldBlock(BlockUtil.posToVec3d(this.pos)));
        }
        if (this.swing.getValue().booleanValue()) {
            this.mc.player.isSwingInProgress = false;
            this.mc.player.swingProgressInt = 0;
            this.mc.player.swingProgress = 0.0f;
            this.mc.player.prevSwingProgress = 0.0f;
        }
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        double[] calc = MathUtil.directionSpeed(this.speed.getValue() / 10.0);
        this.mc.player.motionX = calc[0];
        this.mc.player.motionZ = calc[1];
        if (this.Switch.getValue().booleanValue() && (this.mc.player.getHeldItemMainhand().getItem() == null || !(this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock))) {
            for (int j = 0; j < 9; ++j) {
                if (this.mc.player.inventory.getStackInSlot(j) == null || this.mc.player.inventory.getStackInSlot(j).getCount() == 0 || !(this.mc.player.inventory.getStackInSlot(j).getItem() instanceof ItemBlock)) continue;
                this.mc.player.inventory.currentItem = j;
                break;
            }
        }
        if (mc.gameSettings.keyBindJump.isKeyDown() && mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F && Tower.getValue() && !mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            if (!this.teleported && center.getValue()) {
                this.teleported = true;
                BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                mc.player.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            }
            if (center.getValue() && !this.teleported)
                return;
            if(timed.getValue()){
                if(mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.timer.tickLength = 50f / speedT.getValue().floatValue();
                }else if (mc.player.moveForward >= 0.0F && mc.player.moveStrafing >= 0.0F){
                    mc.timer.tickLength = 50f;
                }
            }else {
                mc.player.setVelocity(0.0, 0.42, 0.0);
            }
            if (towerTimer.passedMs(1500)) {
                mc.player.motionY = -0.28;
                towerTimer.reset();
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event){
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet1 = (CPacketPlayer) event.getPacket();
            if (shouldSpoofPacket) {
                ((AccessorCPacketPlayer) packet1).setYaw(yaw);
                ((AccessorCPacketPlayer) packet1).setPitch(pitch);
                shouldSpoofPacket = false;
            }
        }
    }

    @SubscribeEvent
    public void renderModelRotation(RenderModelEvent event) {
        if (!rotate.getValue()) return;
        if (rotating) {
            event.rotating = true;
            event.pitch = renderPitch;
        }
    }

    private void resetRotation() {
        if (shouldSpoofPacket) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            shouldSpoofPacket = false;
            rotating = false;
        }
    }

    public void lookAtPos(BlockPos block, EnumFacing face) {
        float[] v = RotationUtil.getRotationsBlock(block, face, false);
        float[] v2 = RotationUtil.getRotationsBlock(block.add(0, +0.5, 0), face, false);
        setYawAndPitch(v[0], v[1], v2[1]);
    }


    public void setYawAndPitch(float yaw1, float pitch1, float renderPitch1) {
        yaw = yaw1;
        pitch = pitch1;
        renderPitch = renderPitch1;
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
        shouldSpoofPacket = true;
        rotating = true;
    }


    @Override
    public void onWorldRender() {
        //ToDo fix this cause it don work
        if(render.getValue()){
          //  RenderUtil.drawBlockBox(new BlockPos(blocksToRender), color.getValue(), true, 2f);
        }
        super.onWorldRender();
    }

    private boolean isAir(BlockPos pos) {
        return this.mc.world.getBlockState(pos).getBlock() == Blocks.AIR;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.blocksToRender.clear();
        mc.timer.tickLength = 50f;
    }
}
