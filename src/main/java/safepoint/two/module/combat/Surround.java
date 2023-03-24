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
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.event.events.UpdateWalkingPlayerEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.*;
import safepoint.two.utils.core.ChatUtil;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.crystal.CrystalUtils;
import safepoint.two.utils.math.Pair;
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

    IntegerSetting placeDelay = new IntegerSetting("PlaceDelay", 70, 0, 500, this);
    IntegerSetting multiPlace = new IntegerSetting("MultiPlace", 4, 1, 5, this);
    BooleanSetting extend = new BooleanSetting("Extend", false, this);
    BooleanSetting breakCrystals = new BooleanSetting("BreakCrystals", false, this);
    BooleanSetting useEnderChest = new BooleanSetting("EChest", false, this);
    BooleanSetting onPacket = new BooleanSetting("Packet", false, this);
    BooleanSetting rotate = new BooleanSetting("Rotate", false, this);
    BooleanSetting onlyVisible = new BooleanSetting("onlyVisible", false, this);
    BooleanSetting packetPlace = new BooleanSetting("packetPlace", false, this);
    BooleanSetting antiGhostBlock = new BooleanSetting("antiGhostBlock", false, this);
    BooleanSetting antiSuicideCrystal = new BooleanSetting("antiSuicideCrystal", false, this);
    IntegerSetting breakCrystalsDelay = new IntegerSetting("BreakCrystalsDelay", 50, 0, 1000, this);
    FloatSetting minHealthRemaining = new FloatSetting("MinHealthRemain", 8.0f, 1.0f, 36.0f, this);
    FloatSetting maxCrystalDamage = new FloatSetting("MaxCrystalDamage", 11.0f, 0.0f, 36.0f, this);
    BooleanSetting render = new BooleanSetting("render", false, this);
    BooleanSetting center = new BooleanSetting("center", false, this);
    BooleanSetting disableOnLeaveHole = new BooleanSetting("disableOnLeaveHole", false, this);
    BooleanSetting placeWeb = new BooleanSetting("PlaceWeb", false, this);
    BooleanSetting onShift = new BooleanSetting("OnShift", false, this);

    private final Timer placeTimer = new Timer();
    private final Timer fadeTimer = new Timer();
    private final Timer breakCrystalsTimer = new Timer();
    private final HashMap<BlockPos, Float> toRenderPos = new HashMap<>();
    private final HashMap<BlockPos, Boolean> onPacketPlaceFlagMap = new HashMap<>();
    private BlockPos currentPlayerPos = null;
    private boolean centeredFlag = false;
    private boolean isTickPlacingFlag = false;

    @Override
    public void onDisable() {
        onPacketPlaceFlagMap.clear();
        currentPlayerPos = null;
        centeredFlag = false;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!isTickPlacingFlag && mc.world != null && mc.player != null) {
            if (extend.getValue() && event.getPacket() instanceof SPacketBlockBreakAnim) {
                SPacketBlockBreakAnim packet = ((SPacketBlockBreakAnim) event.getPacket());
                BlockPos playerPos = new BlockPos(Math.floor(mc.player.posX), Math.floor(Math.round(mc.player.posY)), Math.floor(mc.player.posZ));

                if (placePoses(false).contains(packet.getPosition())) {
                    BlockPos extendedPos = new BlockPos((packet.getPosition().x * 2.0f) - playerPos.x, (packet.getPosition().y * 2.0f) - playerPos.y, (packet.getPosition().z * 2.0f) - playerPos.z);
                    extendedPos = BlockUtil.extrudeBlock(extendedPos, EnumFacing.DOWN);

                    if (BlockUtil.isFacePlaceble(extendedPos, EnumFacing.UP, true)) {

                        if (breakCrystals.getValue()) {
                            CrystalUtils.breakBlockingCrystals(mc.world.getBlockState(BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP)).getSelectedBoundingBox(mc.world, BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP)), antiSuicideCrystal.getValue(), minHealthRemaining.getValue(), maxCrystalDamage.getValue(), rotate.getValue());
                        }

                        int prevSlot = mc.player.inventory.currentItem;
                        if (InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
                            InventoryUtil.switchToSlot(InventoryUtil.findBlockInHotBar(Blocks.OBSIDIAN));
                        }
                        else if (useEnderChest.getValue() && InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) {
                            InventoryUtil.switchToSlot(InventoryUtil.findBlockInHotBar(Blocks.ENDER_CHEST));
                        }

                        BlockUtil.placeBlock(extendedPos, EnumFacing.UP, packetPlace.getValue(), false, rotate.getValue());
                        if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE) mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP)))));
                        if (render.getValue()) toRenderPos.put(BlockUtil.extrudeBlock(extendedPos, EnumFacing.UP), 300.0f);

                        InventoryUtil.switchToSlot(prevSlot);
                    }
                }
            }

            if (onPacket.getValue()) {
                if (event.getPacket() instanceof SPacketBlockChange) {

                    BlockPos playerPos = new BlockPos(Math.floor(mc.player.posX), Math.floor(Math.round(mc.player.posY) - 1), Math.floor(mc.player.posZ));
                    if (!placePoses(true).contains(((SPacketBlockChange) event.getPacket()).getBlockPosition())) return;

                    boolean flag1 = false;
                    for (Map.Entry<BlockPos, Boolean> entry : onPacketPlaceFlagMap.entrySet()) {
                        if (BlockUtil.isSameBlockPos(entry.getKey(), ((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                            flag1 = true;
                            if (!entry.getValue()) return;
                            else {
                                onPacketPlaceFlagMap.put(entry.getKey(), false);
                            }
                        }
                    }
                    if (!flag1) {
                        return;
                    }

                    if (!((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) return;

                    Pair<BlockPos, EnumFacing> data = getPlaceData(playerPos, ((SPacketBlockChange) event.getPacket()).getBlockPosition());
                    if (data == null) return;

                    if (!onlyVisible.getValue() && data.equals(playerPos) && BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(data.a, data.b), EnumFacing.UP))) {
                        return;
                    }

                    if ((mc.world.getBlockState(BlockUtil.extrudeBlock(data.a, data.b)).getSelectedBoundingBox(mc.world, BlockUtil.extrudeBlock(data.a, data.b)))
                            .intersects(mc.player.getEntityBoundingBox())) {
                        return;
                    }

                    PlayerUtil.entitiesListFlag = true;
                    boolean flag = false;
                    for (Entity entity : PlayerUtil.entitiesList()) {
                        if (entity == mc.player || !(entity instanceof EntityPlayer)) {
                            continue;
                        }

                        if ((mc.world.getBlockState(BlockUtil.extrudeBlock(data.a, data.b)).getSelectedBoundingBox(mc.world, BlockUtil.extrudeBlock(data.a, data.b)))
                                .intersects(entity.getEntityBoundingBox())) {
                            flag = true;
                        }
                    }
                    PlayerUtil.entitiesListFlag = false;
                    if (flag) {
                        return;
                    }

                    if (onShift.getValue() && !mc.gameSettings.keyBindSneak.isPressed())
                        return;

                    int prevSlot = mc.player.inventory.currentItem;
                    if (InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
                        InventoryUtil.switchToSlot(InventoryUtil.findBlockInHotBar(Blocks.OBSIDIAN));
                    }
                    else if (useEnderChest.getValue() && InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) {
                        InventoryUtil.switchToSlot(InventoryUtil.findBlockInHotBar(Blocks.ENDER_CHEST));
                    }

                    BlockUtil.placeBlock(data.a, data.b, packetPlace.getValue(), false, rotate.getValue());
                    if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE) mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(data.a, data.b), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(data.a, data.b)))));
                    if (render.getValue()) toRenderPos.put(BlockUtil.extrudeBlock(data.a, data.b), 300.0f);

                    InventoryUtil.switchToSlot(prevSlot);
                }

            }
        }
    }

    @Override
    public void onTick() {
        if (mc.world != null && mc.player != null && onPacket.getValue()) {
            for (BlockPos pos : placePoses(true)) {
                if (!mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world, pos)) {
                    onPacketPlaceFlagMap.put(pos, true);
                }
            }
        }

        if (mc.world != null && mc.player != null && breakCrystalsTimer.passedMs(breakCrystalsDelay.getValue()) && breakCrystals.getValue()) {
            for (BlockPos pos : placePoses(true)) {
                CrystalUtils.breakBlockingCrystals(mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos), antiSuicideCrystal.getValue(), minHealthRemaining.getValue(), maxCrystalDamage.getValue(), rotate.getValue());
            }
            breakCrystalsTimer.reset();
        }

        tickPlace();
    }

    private void tickPlace() {
        if (mc.world != null && mc.player != null && placeTimer.passedMs(placeDelay.getValue())) {
            if (render.getValue()) toRenderPos.clear();
            if (onShift.getValue() && !mc.gameSettings.keyBindSneak.isPressed())
                return;

            if (!InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) && (!useEnderChest.getValue() || !InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)))) {
                disableModule();
                return;
            }

            if (center.getValue() && !centeredFlag) {
                PlayerUtil.setCenter();
                centeredFlag = true;
            }

            BlockPos playerPos = new BlockPos(Math.floor(mc.player.posX), Math.floor(Math.round(mc.player.posY) - 1), Math.floor(mc.player.posZ));
            if (currentPlayerPos == null) currentPlayerPos = playerPos;

            if (disableOnLeaveHole.getValue() && !BlockUtil.isSameBlockPos(currentPlayerPos, new BlockPos(Math.floor(mc.player.posX), Math.floor(Math.round(mc.player.posY) - 1), Math.floor(mc.player.posZ)))) {
                disableModule();
                return;
            }

            isTickPlacingFlag = true;
            List<Pair<BlockPos, EnumFacing>> list = new ArrayList<>();

            int index = 0;
            for (Pair<BlockPos, EnumFacing> data : onlyVisible.getValue() ? visiblePlacePos(playerPos) : placePos(playerPos)) {
                if (index >= multiPlace.getValue()) {
                    continue;
                }

                if (!onlyVisible.getValue() && data.equals(playerPos) && BlockUtil.isBlockPlaceable(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(data.a, data.b), EnumFacing.UP))) {
                    continue;
                }

                if (!BlockUtil.isFacePlaceble(data.a, data.b, false)) {
                    continue;
                }

                if ((mc.world.getBlockState(BlockUtil.extrudeBlock(data.a, data.b)).getSelectedBoundingBox(mc.world, BlockUtil.extrudeBlock(data.a, data.b)))
                        .intersects(mc.player.getEntityBoundingBox())) {
                    continue;
                }

                PlayerUtil.entitiesListFlag = true;
                boolean flag = false;
                for (Entity entity : PlayerUtil.entitiesList()) {
                    if (entity == mc.player || !(entity instanceof EntityPlayer)) {
                        continue;
                    }

                    if ((mc.world.getBlockState(BlockUtil.extrudeBlock(data.a, data.b)).getSelectedBoundingBox(mc.world, BlockUtil.extrudeBlock(data.a, data.b)))
                            .intersects(entity.getEntityBoundingBox())) {
                        flag = true;
                    }
                }
                PlayerUtil.entitiesListFlag = false;
                if (flag) {
                    continue;
                }

                list.add(new Pair<>(data.a, data.b));

                if (render.getValue()) toRenderPos.put(BlockUtil.extrudeBlock(data.a, data.b), 300.0f);

                index++;
            }

            if (!list.isEmpty()) {
                int prevSlot = mc.player.inventory.currentItem;
                if (InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
                    InventoryUtil.switchToSlot(InventoryUtil.findBlockInHotBar(Blocks.OBSIDIAN));
                }
                else if (useEnderChest.getValue() && InventoryUtil.isItemInHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) {
                    InventoryUtil.switchToSlot(InventoryUtil.findBlockInHotBar(Blocks.ENDER_CHEST));
                }

                list.forEach(data -> {
                    BlockUtil.placeBlock(data.a, data.b, packetPlace.getValue(), false, rotate.getValue());
                    if (antiGhostBlock.getValue() && mc.playerController.currentGameType != GameType.CREATIVE) mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockUtil.extrudeBlock(data.a, data.b), BlockUtil.getVisibleBlockSide(new Vec3d(BlockUtil.extrudeBlock(data.a, data.b)))));
                });
                placeTimer.reset();

                InventoryUtil.switchToSlot(prevSlot);
            }
            isTickPlacingFlag = false;
        }
    }

    private Pair<BlockPos, EnumFacing>[] placePos(BlockPos pos) {
        return new Pair[] {
                new Pair<>(new BlockPos(pos.x + 1, pos.y, pos.z), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x - 1, pos.y, pos.z), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x, pos.y, pos.z + 1), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x, pos.y, pos.z - 1), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x + 2, pos.y + 1, pos.z), EnumFacing.WEST),
                new Pair<>(new BlockPos(pos.x - 2, pos.y + 1, pos.z), EnumFacing.EAST),
                new Pair<>(new BlockPos(pos.x, pos.y + 1, pos.z + 2), EnumFacing.NORTH),
                new Pair<>(new BlockPos(pos.x, pos.y + 1, pos.z - 2), EnumFacing.SOUTH),
                new Pair<>(pos, EnumFacing.EAST),
                new Pair<>(pos, EnumFacing.WEST),
                new Pair<>(pos, EnumFacing.SOUTH),
                new Pair<>(pos, EnumFacing.NORTH)

        };
    }

    private Pair<BlockPos, EnumFacing>[] visiblePlacePos(BlockPos pos) {
        return new Pair[] {
                new Pair<>(new BlockPos(pos.x + 1, pos.y, pos.z), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x - 1, pos.y, pos.z), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x, pos.y, pos.z + 1), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x, pos.y, pos.z - 1), EnumFacing.UP),
                new Pair<>(new BlockPos(pos.x + 2, pos.y + 1, pos.z), EnumFacing.WEST),
                new Pair<>(new BlockPos(pos.x - 2, pos.y + 1, pos.z), EnumFacing.EAST),
                new Pair<>(new BlockPos(pos.x, pos.y + 1, pos.z + 2), EnumFacing.NORTH),
                new Pair<>(new BlockPos(pos.x, pos.y + 1, pos.z - 2), EnumFacing.SOUTH)
        };
    }

    private Pair<BlockPos, EnumFacing> getPlaceData(BlockPos playerPos, BlockPos pos) {
        if (!onlyVisible.getValue()) {
            if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x + 1, playerPos.y, playerPos.z))) {
                return new Pair<>(playerPos, EnumFacing.WEST);
            }
            else if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x - 1, playerPos.y, playerPos.z))) {
                return new Pair<>(playerPos, EnumFacing.EAST);
            }
            else if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x, playerPos.y, playerPos.z + 1))) {
                return new Pair<>(playerPos, EnumFacing.NORTH);
            }
            else if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x, playerPos.y, playerPos.z - 1))) {
                return new Pair<>(playerPos, EnumFacing.SOUTH);
            }
        }

        if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x + 1, playerPos.y + 1, playerPos.z))) {
            Pair<BlockPos, EnumFacing> toPlacePos1 = new Pair<>(BlockUtil.extrudeBlock(playerPos, EnumFacing.EAST), EnumFacing.UP);
            Pair<BlockPos, EnumFacing> toPlacePos2 = new Pair<>(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(toPlacePos1.a, EnumFacing.EAST), EnumFacing.UP), EnumFacing.WEST);
            if (BlockUtil.isBlockPlaceable(toPlacePos1.a)) {
                return toPlacePos1;
            }
            else {
                return toPlacePos2;
            }
        }
        else if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x - 1, playerPos.y + 1, playerPos.z))) {
            Pair<BlockPos, EnumFacing> toPlacePos1 = new Pair<>(BlockUtil.extrudeBlock(playerPos, EnumFacing.WEST), EnumFacing.UP);
            Pair<BlockPos, EnumFacing> toPlacePos2 = new Pair<>(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(toPlacePos1.a, EnumFacing.WEST), EnumFacing.UP), EnumFacing.EAST);
            if (BlockUtil.isBlockPlaceable(toPlacePos1.a)) {
                return toPlacePos1;
            }
            else {
                return toPlacePos2;
            }
        }
        else if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x, playerPos.y + 1, playerPos.z + 1))) {
            Pair<BlockPos, EnumFacing> toPlacePos1 = new Pair<>(BlockUtil.extrudeBlock(playerPos, EnumFacing.SOUTH), EnumFacing.UP);
            Pair<BlockPos, EnumFacing> toPlacePos2 = new Pair<>(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(toPlacePos1.a, EnumFacing.SOUTH), EnumFacing.UP), EnumFacing.NORTH);
            if (BlockUtil.isBlockPlaceable(toPlacePos1.a)) {
                return toPlacePos1;
            }
            else {
                return toPlacePos2;
            }
        }
        else if (BlockUtil.isSameBlockPos(pos, new BlockPos(playerPos.x, playerPos.y + 1, playerPos.z - 1))) {
            Pair<BlockPos, EnumFacing> toPlacePos1 = new Pair<>(BlockUtil.extrudeBlock(playerPos, EnumFacing.NORTH), EnumFacing.UP);
            Pair<BlockPos, EnumFacing> toPlacePos2 = new Pair<>(BlockUtil.extrudeBlock(BlockUtil.extrudeBlock(toPlacePos1.a, EnumFacing.NORTH), EnumFacing.UP), EnumFacing.SOUTH);
            if (BlockUtil.isBlockPlaceable(toPlacePos1.a)) {
                return toPlacePos1;
            }
            else {
                return toPlacePos2;
            }
        }

        return null;
    }

    private List<BlockPos> placePoses(boolean includeBottom) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(Math.round(mc.player.posY) - 1), Math.floor(mc.player.posZ));

        if (includeBottom) {
            list.add(new BlockPos(pos.x + 1, pos.y, pos.z));
            list.add(new BlockPos(pos.x - 1, pos.y, pos.z));
            list.add(new BlockPos(pos.x, pos.y, pos.z + 1));
            list.add(new BlockPos(pos.x, pos.y, pos.z - 1));
        }
        list.add(new BlockPos(pos.x + 1, pos.y + 1, pos.z));
        list.add(new BlockPos(pos.x - 1, pos.y + 1, pos.z));
        list.add(new BlockPos(pos.x, pos.y + 1, pos.z + 1));
        list.add(new BlockPos(pos.x, pos.y + 1, pos.z - 1));

        return list;
    }
}