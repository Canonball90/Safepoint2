package safepoint.two.module.combat;

import net.minecraft.block.Block;
import net.minecraft.block.*;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.event.events.UpdateWalkingPlayerEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.core.settings.impl.IntegerSetting;
import safepoint.two.utils.core.ChatUtil;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.world.BlockUtil;
import safepoint.two.utils.world.InventoryUtil;
import safepoint.two.utils.world.PlayerUtil;
import safepoint.two.utils.world.RotationUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ModuleInfo(name = "Surround", description = "", category = Module.Category.Combat)
public class Surround extends Module {
    DoubleSetting Delay = new DoubleSetting("Delay", 1, 1, 20, this);
    BooleanSetting SelfWeb = new BooleanSetting("SelfWeb", false, this);
    BooleanSetting CenterSelf = new BooleanSetting("Center", false, this);
    BooleanSetting AutoDisable = new BooleanSetting("AutoDisable", false, this);
    BooleanSetting PacketPLace = new BooleanSetting("PacketPlace", false, this);
    BooleanSetting onlyOnStop = new BooleanSetting("OnStop", false, this);
    BooleanSetting onlyOnSneak = new BooleanSetting("OnSneak", false, this);
    BooleanSetting disableOnJump = new BooleanSetting("DisableOnJump", false, this);
    BooleanSetting predict = new BooleanSetting("Predict", false, this);
    BooleanSetting allowNon1x1 = new BooleanSetting("Non1x1", false, this);
    EnumSetting mode = new EnumSetting("Mode", "RenderTick", Arrays.asList("RenderTick", "PWUpdate", "Thread"), this);
    int del = 0;
    private EntityPlayer target;
    ArrayList<BlockPos> blockChanged = new ArrayList<>();

    public static BlockPos getPosByFloor(){
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }
    @Override
    public void onEnable() {
        del = 0;
    }
    @Override
    public void onDisable() {
        del = 0;
    }
    @SubscribeEvent
    public void onDis(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.disableModule();
    }
    public void Center(){
        BlockPos pos = getPosByFloor();
        mc.player.connection.sendPacket(new CPacketPlayer.Position(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, true));
        mc.player.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.RenderTickEvent event) {
        if (mc.world == null || mc.player == null) {return;}
        if (mode.getValue().equalsIgnoreCase("RenderTick")) {
            doSurround();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdateWalk(UpdateWalkingPlayerEvent event) {
        if (mc.world == null || mc.player == null) {return;}
        if(mode.getValue().equalsIgnoreCase("Thread")){
            try {
                run(() -> doSurround());
            } catch (Exception e) {
                doSurround();
            }
        }else if (mode.getValue().equalsIgnoreCase("PWUpdate")){
            doSurround();
        }

    }

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.Receive event){
        if (event.getPacket() instanceof SPacketBlockChange && this.predict.getValue()) {
            SPacketBlockChange packet = event.getPacket();
            if (!blockChanged.contains(packet.getBlockPosition())) {
                for (BlockPos pos : this.getOffsets()) {
                    if (!pos.equals(packet.getBlockPosition()) || packet.getBlockState().getBlock() != Blocks.AIR)
                        continue;
                    int blockSlot = PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));
                    if (blockSlot == -1) {
                        return;
                    }
                    if (blockSlot != mc.player.inventory.currentItem)
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(blockSlot));
                    PlayerUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.PacketPLace.getValue());
                    if (blockSlot != mc.player.inventory.currentItem) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                        mc.playerController.updateController();
                    }
                    blockChanged.add(pos);
                    break;
                }
            }
        }
    }

    void doSurround(){
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;
        if (onlyOnStop.getValue() && (mc.player.motionX != 0 || mc.player.motionY != 0 || mc.player.motionZ != 0))
            return;

        if (onlyOnSneak.getValue() && !mc.gameSettings.keyBindSneak.isPressed())
            return;

        if (disableOnJump.getValue() && Math.abs(Math.abs(y) - Math.abs(mc.player.posY)) >= 0.3) {
            return;
        }
        if (PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
            del++;
            if(!predict.getValue()) {
                if (del == Delay.getValue()) {
                    if (CenterSelf.getValue()) {
                        Center();
                    }

                    if (mc.world.getBlockState(new BlockPos(x + 1, y, z)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x + 1, y, z), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x - 1, y, z)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x - 1, y, z), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x, y, z + 1)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x, y, z + 1), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x, y, z - 1)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x, y, z - 1), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x + 1, y - 1, z)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x + 1, y - 1, z), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x - 1, y - 1, z)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x - 1, y - 1, z), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x, y - 1, z + 1)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x, y - 1, z + 1), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x, y - 1, z - 1)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x, y - 1, z - 1), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (mc.world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.AIR) {
                        PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)));
                        PlayerUtil.placeBlock(new BlockPos(x, y - 1, z), EnumHand.MAIN_HAND, PacketPLace.getValue());
                        del = 0;
                    }

                    if (SelfWeb.getValue()) {
                        if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR) {
                            if (PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.WEB)) != -1) {
                                int prevSlot = mc.player.inventory.currentItem;
                                PlayerUtil.changeSlot(PlayerUtil.getSlot(Item.getItemFromBlock(Blocks.WEB)));
                                PlayerUtil.placeBlock(new BlockPos(PlayerUtil.getPosFloored(mc.player)), EnumHand.MAIN_HAND, false);
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(prevSlot));
                            } else {
                                ChatUtil.send("You need to have webs to selfweb");
                                this.disableModule();
                            }
                        }
                    }
                    if (AutoDisable.getValue()) {
                        this.disableModule();
                    }
                    del = 0;
                }
            }
        }else {
            ChatUtil.send("You need to have obsidian to surround");
            this.disableModule();
        }
    }

    List<BlockPos> getOffsets() {
        BlockPos playerPos = this.getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<BlockPos>();
        if (this.allowNon1x1.getValue()) {
            int z;
            int x;
            double decimalX = Math.abs(mc.player.posX) - Math.floor(Math.abs(mc.player.posX));
            double decimalZ = Math.abs(mc.player.posZ) - Math.floor(Math.abs(mc.player.posZ));
            int lengthXPos = this.calcLength(decimalX, false);
            int lengthXNeg = this.calcLength(decimalX, true);
            int lengthZPos = this.calcLength(decimalZ, false);
            int lengthZNeg = this.calcLength(decimalZ, true);
            ArrayList<BlockPos> tempOffsets = new ArrayList<BlockPos>();
            offsets.addAll(this.getOverlapPos());
            for (x = 1; x < lengthXPos + 1; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }
            for (x = 0; x <= lengthXNeg; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
            }
            for (z = 1; z < lengthZPos + 1; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }
            for (z = 0; z <= lengthZNeg; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
            }
            for (BlockPos pos : tempOffsets) {
                if (getDown(pos)) {
                    offsets.add(pos.add(0, -1, 0));
                }
                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));
            for (int[] surround : new int[][]{
                    {1, 0},
                    {0, 1},
                    {-1, 0},
                    {0, -1}
            }) {
                if (getDown(playerPos.add(surround[0], 0, surround[1])))
                    offsets.add(playerPos.add(surround[0], -1, surround[1]));

                offsets.add(playerPos.add(surround[0], 0, surround[1]));
            }
        }
        return offsets;
    }

    int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
    }

    BlockPos getPlayerPos() {
        double decimalPoint = mc.player.posY - Math.floor(mc.player.posY);
        return new BlockPos(mc.player.posX, decimalPoint > 0.8 ? Math.floor(mc.player.posY) + 1.0 : Math.floor(mc.player.posY), mc.player.posZ);
    }

    public static boolean getDown(BlockPos pos) {

        for (EnumFacing e : EnumFacing.values())
            if (!mc.world.isAirBlock(pos.add(e.getDirectionVec())))
                return false;

        return true;

    }

    BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(x, y, z);
    }

    List<BlockPos> getOverlapPos() {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        double decimalX = mc.player.posX - Math.floor(mc.player.posX);
        double decimalZ = mc.player.posZ - Math.floor(mc.player.posZ);
        int offX = this.calcOffset(decimalX);
        int offZ = this.calcOffset(decimalZ);
        positions.add(this.getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(this.getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }

    int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    protected ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void run(Runnable command) {
        try {
            executorService.execute(command);
        } catch (Exception ignored){
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}