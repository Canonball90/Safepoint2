package safepoint.two.module.player;

import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import safepoint.two.core.event.events.EventPlayerDamageBlock;
import safepoint.two.core.event.events.LeftClickBlockEvent;
import safepoint.two.core.event.events.UpdateWalkingPlayerEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.core.settings.impl.IntegerSetting;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.world.BlockUtil;
import safepoint.two.utils.world.InventoryUtil;

import java.awt.*;
import java.util.Arrays;

@ModuleInfo(name = "PacketMine", category = Module.Category.Player, description = "Throws Exp using packets")
public class PacketMine extends Module {
    private BlockPos miningPos = null;
    private EnumFacing miningFace = EnumFacing.UP;
    private int prevSlot = 0;
    private boolean flag = false;
    private boolean flag2 = false;
    private boolean flag3 = false;
    private boolean instantMineRotationFlag = false;
    private boolean onMined = false;
    private final Timer instantMineTimer = new Timer();

    EnumSetting mode = new EnumSetting("Modr", "Packet", Arrays.asList("Packet", "Insta"), this);
    EnumSetting swap = new EnumSetting("Swap", "Normal", Arrays.asList("Normal", "None", "Silent"), this);
    BooleanSetting oppositeFaceHit = new BooleanSetting("OppositeFaceHit", false, this);
    BooleanSetting spamPackets = new BooleanSetting("SpamPackets", false, this);
    IntegerSetting instantMineDelay = new IntegerSetting("InstantMineDelay", 70, 0, 1000, this);
    FloatSetting range = new FloatSetting("Range",  8.0f, 1.0f, 10.0f, this);
    BooleanSetting rotate = new BooleanSetting("Rotate", false, this);

    @Override
    public void onDisable() {
        BlockUtil.packetMiningFlag = false;
        miningPos = null;
        moduleDisableFlag = true;
    }

//    @Override
//    public void onWorldRender() {
//        if((miningPos == null || !BlockUtil.packetMiningFlag)) return;
//        RenderUtil.drawBlockBox(miningPos, new Color(50, 255, 40, 130), true, 3f);
//        super.onWorldRender();
//    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (spamPackets.getValue() && miningPos != null && BlockUtil.isBlockPlaceable(miningPos) && mode.getValue().equalsIgnoreCase("Packet") && BlockUtil.packetMiningFlag) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, miningPos, miningFace));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, miningPos, miningFace));
        }
    }

    @Override
    public void onTick() {
        if (miningPos != null) {
            if (!BlockUtil.isBlockPlaceable(miningPos)) {
                BlockUtil.packetMiningFlag = false;
                miningPos = null;
                return;
            }
            else {
                miningFace = BlockUtil.getVisibleBlockSide(new Vec3d(miningPos.x + 0.5, miningPos.y, miningPos.z + 0.5));
            }

            if (getDistance(mc.player.getPositionVector(), new Vec3d(miningPos)) > range.getValue()) {
                miningPos = null;
                return;
            }

            switch (mode.getValue()) {
                case "Packet": {
                    int bestSlot = InventoryUtil.fastestMiningTool(mc.world.getBlockState(miningPos).getBlock());

                    if (remainingTime() <= 0 && remainingTime() >= -750) {
                        if (!flag) {
                            flag = true;
                            prevSlot = mc.player.inventory.currentItem;
                        }

                        switch (swap.getValue()) {
                            case "Normal": {
                                InventoryUtil.switchToSlot(bestSlot);
                                onMined = true;
                                break;
                            }

                            case "Silent": {
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(bestSlot));
                                onMined = true;
                                break;
                            }
                        }
                    }

                    if (!flag2 && remainingTime() <= 0 && BlockUtil.isBlockPlaceable(miningPos) && swap.getValue().equalsIgnoreCase("None") && mc.player.inventory.currentItem == bestSlot) {
                        onMined = true;
                        flag2 = true;
                    }

                    if (swap.getValue().equalsIgnoreCase("None") && !BlockUtil.isBlockPlaceable(miningPos)) {
                        miningPos = null;
                    }
                    break;
                }

                case "Insta": {
                    if (mc.playerController != null) {
                        if (remainingTime() <= 0) {
                            if (BlockUtil.isBlockPlaceable(miningPos) && instantMineTimer.passedMs(instantMineDelay.getValue())) {
                                int bestSlot = InventoryUtil.fastestMiningTool(mc.world.getBlockState(miningPos).getBlock());

                                instantMineRotationFlag = true;

                                if (!flag3) {
                                    prevSlot = mc.player.inventory.currentItem;
                                }

                                switch (swap.getValue()) {
                                    case "Normal": {
                                        InventoryUtil.switchToSlot(bestSlot);
                                        flag3 = true;
                                        break;
                                    }

                                    case "Silent": {
                                        mc.player.connection.sendPacket(new CPacketHeldItemChange(bestSlot));
                                        flag3 = true;
                                        break;
                                    }
                                }

                                mc.playerController.isHittingBlock = false;
                                mc.playerController.blockHitDelay = 0;
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, miningPos, miningFace));
                                instantMineTimer.reset();
                            }
                            else if (flag3) {
                                switch (swap.getValue()) {
                                    case "Normal": {
                                        InventoryUtil.switchToSlot(prevSlot);
                                        break;
                                    }

                                    case "Silent": {
                                        mc.player.connection.sendPacket(new CPacketHeldItemChange(prevSlot));
                                        break;
                                    }
                                }
                                flag3 = false;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(LeftClickBlockEvent event) {
        if (miningPos != null && event.blockPos == miningPos) {
            event.setCanceled(true);
        }

        BlockPos tempMiningPos = miningPos == null ? new BlockPos(0, -99999, 0) : miningPos;

        if (mc.world.getBlockState(event.blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(event.blockPos).getBlock() != Blocks.BARRIER && (remainingTime() <= 0.0 || ((event.blockPos.x != tempMiningPos.x) || (event.blockPos.y != tempMiningPos.y) || (event.blockPos.z != tempMiningPos.z)))) {

            BlockUtil.mineBlock(event.blockPos, event.face, true);

            if (oppositeFaceHit.getValue()) {
                BlockUtil.mineBlock(event.blockPos, event.face.getOpposite(), true);
            }

            miningPos = event.blockPos;
            BlockUtil.packetMiningFlag = true;
            flag2 = false;
        }
    }

    @SubscribeEvent
    public void onDamageBlock(EventPlayerDamageBlock event) {
//        if (mode.getValue().equalsIgnoreCase("Packet") && event.getPos() == miningPos) {
//            event.cancel();
//        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (mode.getValue().equalsIgnoreCase("Packet") && miningPos != null && miningFace != null && mode.getValue().equalsIgnoreCase("Packet") && onMined) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, miningPos, miningFace));
            mc.player.swingArm(EnumHand.MAIN_HAND);

            if (rotate.getValue()) {
                rotate();
            }

            if (swap.getValue().equalsIgnoreCase("None")) {
                int bestSlot = InventoryUtil.fastestMiningTool(mc.world.getBlockState(miningPos).getBlock());
                miningPos = null;

                if (flag) {
                    if (prevSlot != bestSlot) {
                        switch (swap.getValue()) {
                            case "Normal": {
                                InventoryUtil.switchToSlot(prevSlot);
                                break;
                            }

                            case "Silent": {
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(prevSlot));
                                break;
                            }
                        }
                    }
                    flag = false;
                }
            }

            onMined = false;
        }

        if (instantMineRotationFlag && rotate.getValue()) {
            rotate();
            instantMineRotationFlag = false;
        }
    }

    private void rotate() {

    }

    private double remainingTime() {
        if (miningPos == null) return 0.0;
        int bestSlot = InventoryUtil.fastestMiningTool(mc.world.getBlockState(miningPos).getBlock());
        return BlockUtil.packetMineStartTime + BlockUtil.blockBrokenTime(miningPos, mc.player.inventory.getStackInSlot(bestSlot)) - System.currentTimeMillis();
    }

    public double getDistance(Vec3d start, Vec3d end) {
        double x = end.x - start.x;
        double y = end.y - start.y;
        double z = end.z - start.z;
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

}
