package safepoint.two.utils.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import safepoint.two.Safepoint;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import safepoint.two.core.initializers.RotationInitializer;

import java.util.*;

import static net.minecraft.init.Enchantments.EFFICIENCY;
import static safepoint.two.Safepoint.mc;

public class BlockUtil {

    public static long packetMineStartTime = 0L;
    public static boolean packetMiningFlag = false;
    public static boolean isPlacing = false;


    public enum AirMode {
        NoAir,
        AirOnly,
        Ignored
    }

    public static List<BlockPos> getBlocksInRadius(double radius, AirMode airMode) {
        ArrayList<BlockPos> posList = new ArrayList<>();
        BlockPos pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
        for (int x = pos.getX() - (int) radius; x <= pos.getX() + radius; ++x) {
            for (int y = pos.getY() - (int) radius; y < pos.getY() + radius; ++y) {
                for (int z = pos.getZ() - (int) radius; z <= pos.getZ() + radius; ++z) {
                    double distance = (pos.getX() - x) * (pos.getX() - x) + (pos.getZ() - z) * (pos.getZ() - z) + (pos.getY() - y) * (pos.getY() - y);
                    BlockPos position = new BlockPos(x, y, z);
                    if (distance < radius * radius) {
                        if (airMode.equals(AirMode.NoAir) && mc.world.getBlockState(position).getBlock().equals(Blocks.AIR))
                            continue;
                        if (airMode.equals(AirMode.AirOnly) && !mc.world.getBlockState(position).getBlock().equals(Blocks.AIR))
                            continue;
                        posList.add(position);
                    }
                }
            }
        }
        return posList;
    }

    public static void placeBlock(BlockPos blockPos, boolean packet) {
        for (EnumFacing enumFacing : EnumFacing.values()) {
            if (!(mc.world.getBlockState(blockPos.offset(enumFacing)).equals(Blocks.AIR))) {
                for (Entity entity : mc.world.loadedEntityList)
                    if (new AxisAlignedBB(blockPos).intersects(entity.getEntityBoundingBox()))
                        return;

                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

                if (packet)
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(blockPos.offset(enumFacing), enumFacing.getOpposite(), EnumHand.MAIN_HAND, 0, 0, 0));
                else
                    mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos.offset(enumFacing), enumFacing.getOpposite(), new Vec3d(blockPos), EnumHand.MAIN_HAND);

                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                return;
            }
        }
    }

    public static void rotatePacket(final double x, final double y, final double z) {
        final double diffX = x - Minecraft.getMinecraft().player.posX;
        final double diffY = y - (Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight());
        final double diffZ = z - Minecraft.getMinecraft().player.posZ;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        Minecraft.getMinecraft().player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(yaw, pitch, Minecraft.getMinecraft().player.onGround));
    }

    public static float calculateEntityDamage(final Entity crystal, final EntityPlayer entityPlayer) {
        return calculatePosDamage(crystal.posX, crystal.posY, crystal.posZ, (Entity)entityPlayer);
    }

    public static float calculatePosDamage(final BlockPos position, final EntityPlayer entityPlayer) {
        return calculatePosDamage(position.getX() + 0.5, position.getY() + 1.0, position.getZ() + 0.5, (Entity)entityPlayer);
    }

    public static float calculatePosDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleSize = 12.0f;
        final double size = entity.getDistance(posX, posY, posZ) / 12.0;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double value = (1.0 - size) * blockDensity;
        final float damage = (float)(int)((value * value + value) / 2.0 * 7.0 * 12.0 + 1.0);
        double finalDamage = 1.0;
        if (entity instanceof EntityLivingBase) {
            finalDamage = getBlastReduction((EntityLivingBase)entity, getMultipliedDamage(damage), new Explosion((World)mc.world, (Entity)null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finalDamage;
    }

    private static float getMultipliedDamage(final float damage) {
        return damage * ((mc.world.getDifficulty().getId() == 0) ? 0.0f : ((mc.world.getDifficulty().getId() == 2) ? 1.0f : ((mc.world.getDifficulty().getId() == 1) ? 0.5f : 1.5f)));
    }

    public static float getBlastReduction(final EntityLivingBase entity, final float damageI, final Explosion explosion) {
        float damage = damageI;
        final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        int k = 0;
        try {
            k = EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), ds);
        }
        catch (Exception ex) {}
        damage *= 1.0f - MathHelper.clamp((float)k, 0.0f, 20.0f) / 25.0f;
        if (entity.isPotionActive(MobEffects.RESISTANCE)) {
            damage -= damage / 4.0f;
        }
        return damage;
    }


    public static BlockPos extrudeBlock(BlockPos pos, EnumFacing direction) {

        switch (direction) {
            case WEST: return new BlockPos(pos.x - 1.0, pos.y, pos.z);

            case EAST: return new BlockPos(pos.x + 1.0, pos.y, pos.z);

            case NORTH: return new BlockPos(pos.x, pos.y, pos.z - 1.0);

            case SOUTH: return new BlockPos(pos.x, pos.y, pos.z + 1.0);

            case UP: return new BlockPos(pos.x, pos.y + 1.0, pos.z);

            case DOWN: return new BlockPos(pos.x, pos.y - 1.0, pos.z);
        }

        return pos;
    }

    public static boolean isBlockPlaceable(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block != Blocks.AIR && block != Blocks.WATER && block != Blocks.FLOWING_WATER && block != Blocks.LAVA && block != Blocks.FLOWING_LAVA;
    }

    public static boolean isFacePlaceble(BlockPos pos, EnumFacing facing, boolean checkEntity) {
        BlockPos pos1 = BlockUtil.extrudeBlock(pos, facing);
        return !mc.world.getBlockState(pos).getMaterial().isReplaceable() && mc.world.getBlockState(pos1).getMaterial().isReplaceable() && (!checkEntity || PlayerUtil.isPosPlaceable(pos1));
    }
    public static Vec3d getBlockVecFaceCenter(BlockPos blockPos, EnumFacing face) {
        BlockPos pos = new BlockPos(Math.floor(blockPos.x), Math.floor(blockPos.y), Math.floor(blockPos.z));
        switch (face) {
            case UP: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y + 1.0,
                        pos.z + 0.5
                );
            }

            case DOWN: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y,
                        pos.z + 0.5
                );
            }

            case EAST: {
                return new Vec3d(
                        pos.x + 1.0,
                        pos.y + 0.5,
                        pos.z + 0.5
                );
            }

            case WEST: {
                return new Vec3d(
                        pos.x,
                        pos.y + 0.5,
                        pos.z + 0.5
                );
            }

            case NORTH: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y + 0.5,
                        pos.z + 1.0
                );
            }

            case SOUTH: {
                return new Vec3d(
                        pos.x + 0.5,
                        pos.y + 0.5,
                        pos.z
                );
            }
        }

        return new Vec3d(0, 0, 0);
    }

    public static EnumFacing getVisibleBlockSide(Vec3d blockVec) {
        Vec3d eyeVec = mc.player.getPositionEyes(mc.getRenderPartialTicks()).subtract(blockVec);
        return EnumFacing.getFacingFromVector((float)eyeVec.x, (float)eyeVec.y, (float)eyeVec.z);
    }

    public static Vec3d getVec3dBlock(BlockPos blockPos, EnumFacing face) {
        return new Vec3d(blockPos).add(0.5, 0.5, 0.5).add(new Vec3d(face.getDirectionVec()).scale(0.5));
    }

    public static float blockBreakSpeed(IBlockState blockMaterial, ItemStack tool) {
        float mineSpeed = tool.getDestroySpeed(blockMaterial);
        int efficiencyFactor = EnchantmentHelper.getEnchantmentLevel(EFFICIENCY, tool);

        mineSpeed = (float) (mineSpeed > 1.0 && efficiencyFactor > 0 ? (efficiencyFactor * efficiencyFactor + mineSpeed + 1.0) : mineSpeed);

        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            mineSpeed *= 1.0f + Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.HASTE)).getAmplifier() * 0.2f;
        }

        if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            switch (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE)).getAmplifier()) {
                case 0 : {
                    mineSpeed *= 0.3f;
                    break;
                }

                case 1: {
                    mineSpeed *= 0.09f;
                    break;
                }

                case 2: {
                    mineSpeed *= 0.0027f;
                    break;
                }

                default: {
                    mineSpeed *= 0.00081f;
                }
            }
        }

        if (!mc.player.onGround || (mc.player.isInWater() && EnchantmentHelper.getEnchantmentLevel(Enchantments.AQUA_AFFINITY, mc.player.inventory.armorItemInSlot(0)) == 0)) {
            mineSpeed /= 5.0f;
        }

        return mineSpeed;
    }

    public static double blockBrokenTime(BlockPos pos, ItemStack tool) {
        IBlockState blockMaterial = mc.world.getBlockState(pos);
        float damageTicks = blockBreakSpeed(blockMaterial, tool) /
                blockMaterial.getBlockHardness(mc.world, pos) / 30.0f;
        return (Math.ceil(1.0f / damageTicks) * 50.0f) * (20.0f / Safepoint.serverInitializer.getTPS());
    }

    public static void placeBlock(BlockPos pos, EnumFacing facing, boolean packetPlace, boolean offHand, boolean spoofRotate) {
        isPlacing = true;

        if (!mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }

        Vec3d blockVec = getVec3dBlock(pos, facing);

        if (spoofRotate) {
            float[] r = RotationUtil.getRotationsBlock(pos, facing, true);
            RotationInitializer.setYawAndPitchBlock(r[0], r[1]);
        }

        if (packetPlace) {
            float x = (float)(blockVec.x - pos.getX());
            float y = (float)(blockVec.y - pos.getY());
            float z = (float)(blockVec.z - pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, x, y, z));
        }
        else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, blockVec, offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        }

        mc.player.swingArm(EnumHand.MAIN_HAND);

        if (spoofRotate) {
            RotationInitializer.resetRotationBlock();
        }

        if (!mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
            mc.playerController.updateController();
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            mc.player.setSneaking(false);
            mc.playerController.updateController();
        }


        isPlacing = false;
    }

    public static void mineBlock(BlockPos pos, EnumFacing face, boolean packetMine) {
        if (packetMine) {
            packetMineStartTime = System.currentTimeMillis();
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, face));
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
        else if (mc.playerController.onPlayerDamageBlock(pos, face)) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    public static boolean isSameBlockPos(BlockPos pos1, BlockPos pos2) {
        AxisAlignedBB bb1 = getBoundingFromPos(pos1);
        AxisAlignedBB bb2 = getBoundingFromPos(pos2);
        return bb1.maxX == bb2.maxX && bb1.maxY == bb2.maxY && bb1.maxZ == bb2.maxZ;
    }

    public static List<EnumFacing> getVisibleSides(BlockPos pos) {
        List<EnumFacing> list = new ArrayList<>();
        boolean isFullBox = !mc.world.getBlockState(pos).isFullBlock() || !mc.world.isAirBlock(pos);
        Vec3d eyesVec = mc.player.getPositionEyes(mc.getRenderPartialTicks());
        Vec3d blockVec = new Vec3d(pos).add(0.5, 0.5, 0.5);
        double diffX = eyesVec.x - blockVec.x;
        double diffY = eyesVec.y - blockVec.y;
        double diffZ = eyesVec.z - blockVec.z;

        if (diffX < -0.5) {
            list.add(EnumFacing.WEST);
        } else if (diffX > 0.5) {
            list.add(EnumFacing.EAST);
        } else if (isFullBox) {
            list.add(EnumFacing.WEST);
            list.add(EnumFacing.EAST);
        }

        if (diffY < -0.5) {
            list.add(EnumFacing.DOWN);
        } else if (diffY > 0.5) {
            list.add(EnumFacing.UP);
        } else {
            list.add(EnumFacing.DOWN);
            list.add(EnumFacing.UP);
        }

        if (diffZ < -0.5) {
            list.add(EnumFacing.NORTH);
        } else if (diffZ > 0.5) {
            list.add(EnumFacing.SOUTH);
        } else if (isFullBox) {
            list.add(EnumFacing.NORTH);
            list.add(EnumFacing.SOUTH);
        }

        return list;
    }

    public static AxisAlignedBB getBoundingFromPos(BlockPos pos) {
        IBlockState iBlockState = mc.world.getBlockState(pos);
        return iBlockState.getSelectedBoundingBox(mc.world, pos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);
    }

    public static Vec3d posToVec3d(BlockPos pos) {
        return new Vec3d(pos);
    }

    public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
        EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }
        if (rotate) {
        }
        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.rightClickDelayTimer = 4;
    }

    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, 0.5f, 1.0f, 0.5f));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
        }
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        mc.rightClickDelayTimer = 4;
    }

    public static List<Block> blackList;
    public static List<Block> shulkerList;

    static {
        blackList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER);
        shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);
    }

    public static EnumFacing getFirstFacing(BlockPos pos) {
        Iterator<EnumFacing> iterator = getPossibleSides(pos).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public static List<EnumFacing> getPossibleSides(BlockPos pos) {
        ArrayList<EnumFacing> facings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false) || mc.world.getBlockState(neighbour).getMaterial().isReplaceable())
                continue;
            facings.add(side);
        }
        return facings;
    }



}
