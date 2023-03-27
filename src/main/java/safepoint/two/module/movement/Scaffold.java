package safepoint.two.module.movement;

import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.ColorSetting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.world.BlockUtil;
import safepoint.two.utils.world.ScaffoldBlock;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Scaffold", description = "", category = Module.Category.Movement)
public class Scaffold extends Module {

    BooleanSetting rotate = new BooleanSetting("Rotate", true, this);
    BooleanSetting render = new BooleanSetting("Render", true, this);
    BooleanSetting swing = new BooleanSetting("Swing", true, this);
    BooleanSetting Switch = new BooleanSetting("Switch", true, this);
    BooleanSetting Tower = new BooleanSetting("Tower", true, this);
    BooleanSetting NCP = new BooleanSetting("NCP", true, this);
    BooleanSetting NCPJumo = new BooleanSetting("NCPJump", true, this);
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

    @Override
    public void onTick() {
        if(mc.player == null || mc.world == null) return;
        this.pos = new BlockPos(this.mc.player.posX, this.mc.player.posY - 1.0, this.mc.player.posZ);
        if(isAir(this.pos)){
            mc.player.setSneaking(true);
        }else{
            mc.player.setSneaking(false);
        }
        if (this.isAir(this.pos)) {//ToDo add , this.mc.player.isSneaking() later
            BlockUtil.placeBlock(this.pos, EnumFacing.UP, swing.getValue(), false, rotate.getValue());
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
        if (NCP.getValue()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                if (NCPJumo.getValue()) {
                    mc.player.jump();
                } else {
                    mc.player.motionY = 0.2;
                }

                mc.player.motionX *= 0.3;
                mc.player.motionZ *= 0.3;

                if (towerTimer.passedMs(1200L)) {
                    towerTimer.reset();
                    mc.player.motionY = -0.28;
                }
            }
        }else {
            if (mc.gameSettings.keyBindJump.isKeyDown() && mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F && Tower.getValue() && !mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                if (!this.teleported && center.getValue()) {
                    this.teleported = true;
                    BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    mc.player.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                }
                if (center.getValue() && !this.teleported)
                    return;
                mc.player.motionY = upSpeed.getValue();
                mc.player.motionZ = 0.0D;
                mc.player.motionX = 0.0D;
                if (this.time.passedMs(1500L)) {
                    //ToDo add timer manager
                    Safepoint.serverInitializer.reset();
                    time.reset();
                    mc.player.motionY = -0.28D;
                }
            }
        }
    }

    @Override
    public void onWorldRender() {
        //ToDo fix this cause it don work
        if(render.getValue()){
            RenderUtil.drawBlockBox(new BlockPos(blocksToRender), color.getValue(), true, 2f);
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
    }
}
