package safepoint.two.utils.crystal;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import safepoint.two.mixin.mixins.AccessorCPacketUseEntity;
import safepoint.two.utils.world.BlockUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static safepoint.two.Safepoint.mc;

public class CrystalUtils {

    public static void placeCrystalOnBlock(BlockPos pos, EnumHand hand) {
        RayTraceResult result = Minecraft.getMinecraft().world
                .rayTraceBlocks(
                        new Vec3d(
                                Minecraft.getMinecraft().player.posX,
                                Minecraft.getMinecraft().player.posY
                                        + (double) Minecraft.getMinecraft().player.getEyeHeight(),
                                Minecraft.getMinecraft().player.posZ),
                        new Vec3d((double) pos.getX() + 0.5, (double) pos.getY() - 0.5, (double) pos.getZ() + 0.5));
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        BlockUtil.rotatePacket(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5);
        Minecraft.getMinecraft().player.connection
                .sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0f, 0.0f, 0.0f));
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(Minecraft.getMinecraft().player.posX),
                Math.floor(Minecraft.getMinecraft().player.posY), Math.floor(Minecraft.getMinecraft().player.posZ));
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0F;
        double distancedsize = entity.getDistance(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald = 1.0D;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage),
                    new Explosion(Minecraft.getMinecraft().world, null, posX, posY, posZ, 6.0F, false, true));
        }

        return (float) finald;
    }

    private static float getDamageMultiplied(float damage) {
        int diff = Minecraft.getMinecraft().world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
    }

    public static float calcDmg(BlockPos b, EntityPlayer target) {
        return calculateDamage(b.getX() + .5, b.getY() + 1, b.getZ() + .5, target);
    }

    public static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer) entity;
            final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(),
                    (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            final int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            final float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Objects.requireNonNull(Potion.getPotionById(11)))) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(),
                (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static List<BlockPos> getSphere(BlockPos pos, float r, float h, boolean hollow, boolean sphere, int plusY) {
        List<BlockPos> blocks = new ArrayList<>();
        for(int x = pos.getX() - (int) r; x <= pos.getX() + r; x++) {
            for(int y = (sphere ? pos.getY() - (int) r : pos.getY()); y < (sphere ? pos.getY() + r : pos.getY() + h); y++) {
                for(int z = pos.getZ() - (int) r; z <= pos.getZ() + r; z++) {
                    double dist = (pos.getX() - x) * (pos.getX() - x) + (pos.getZ() - z) * (pos.getZ() - z) + (sphere ? (pos.getY() - y) * (pos.getY() - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        blocks.add(new BlockPos(x, y + plusY, z));
                    }
                }
            }
        }
        return blocks;
    }

    public static List<BlockPos> getSphere(BlockPos pos, float r, boolean hollow) {
        return getSphere(pos, r, (int) r, hollow, true, 0);
    }

    public static float getDamageFromExplosion(Entity entity, Vec3d vector, boolean blockDestruction) {
        return calculateExplosionDamage(entity, vector, 6, blockDestruction);
    }

    public static float getScaledDamage(float damage) {
        World world = mc.world;
        if (world == null) {
            return damage;
        }

        switch (mc.world.getDifficulty()) {
            case PEACEFUL:
                return 0;
            case EASY:
                return Math.min(damage / 2.0F + 1.0F, damage);
            case NORMAL:
            default:
                return damage;
            case HARD:
                return damage * 3.0F / 2.0F;
        }
    }


    public static float calculateExplosionDamage(Entity entity, Vec3d vector, float explosionSize, boolean blockDestruction) {

        double doubledExplosionSize = explosionSize * 2.0;
        double dist = entity.getDistance(vector.x, vector.y, vector.z) / doubledExplosionSize;
        if (dist > 1) {
            return 0;
        }

        double v = (1 - dist) * getBlockDensity(blockDestruction, vector, entity.getEntityBoundingBox());
        float damage = CombatRules.getDamageAfterAbsorb(getScaledDamage((float) ((v * v + v) / 2.0 * 7.0 * doubledExplosionSize + 1.0)), ((EntityLivingBase) entity).getTotalArmorValue(), (float) ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        DamageSource damageSource = DamageSource.causeExplosionDamage(new Explosion(entity.world, entity, vector.x, vector.y, vector.z, (float) doubledExplosionSize, false, true));

        int n = EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), damageSource);
        if (n > 0) {
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, n);
        }

        if (((EntityLivingBase) entity).isPotionActive(MobEffects.RESISTANCE)) {
            PotionEffect potionEffect = ((EntityLivingBase) entity).getActivePotionEffect(MobEffects.RESISTANCE);
            if (potionEffect != null) {
                damage = damage * (25.0F - (potionEffect.getAmplifier() + 1) * 5) / 25.0F;
            }
        }

        return Math.max(damage, 0);
    }

    public static double getBlockDensity(boolean blockDestruction, Vec3d vector, AxisAlignedBB bb) {

        double diffX = 1 / ((bb.maxX - bb.minX) * 2D + 1D);
        double diffY = 1 / ((bb.maxY - bb.minY) * 2D + 1D);
        double diffZ = 1 / ((bb.maxZ - bb.minZ) * 2D + 1D);
        double diffHorizontal = (1 - Math.floor(1D / diffX) * diffX) / 2D;
        double diffTranslational = (1 - Math.floor(1D / diffZ) * diffZ) / 2D;

        if (diffX >= 0 && diffY >= 0 && diffZ >= 0) {

            float solid = 0;
            float nonSolid = 0;

            for (double x = 0; x <= 1; x = x + diffX) {
                for (double y = 0; y <= 1; y = y + diffY) {
                    for (double z = 0; z <= 1; z = z + diffZ) {

                        double scaledDiffX = bb.minX + (bb.maxX - bb.minX) * x;
                        double scaledDiffY = bb.minY + (bb.maxY - bb.minY) * y;
                        double scaledDiffZ = bb.minZ + (bb.maxZ - bb.minZ) * z;

                        if (!isSolid(new Vec3d(scaledDiffX + diffHorizontal, scaledDiffY, scaledDiffZ + diffTranslational), vector, blockDestruction)) {
                            solid++;
                        }

                        nonSolid++;
                    }
                }
            }

            return solid / nonSolid;
        } else {
            return 0;
        }
    }

    public static boolean isSolid(Vec3d start, Vec3d end, boolean blockDestruction) {

        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z)) {
            if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {

                int currX = MathHelper.floor(start.x);
                int currY = MathHelper.floor(start.y);
                int currZ = MathHelper.floor(start.z);

                int endX = MathHelper.floor(end.x);
                int endY = MathHelper.floor(end.y);
                int endZ = MathHelper.floor(end.z);

                BlockPos blockPos = new BlockPos(currX, currY, currZ);
                IBlockState blockState = mc.world.getBlockState(blockPos);
                Block block = blockState.getBlock();

                if ((blockState.getCollisionBoundingBox(mc.world, blockPos) != Block.NULL_AABB) && block.canCollideCheck(blockState, false) && !blockDestruction) {
                    RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);

                    return collisionInterCheck != null;
                }

                double seDeltaX = end.x - start.x;
                double seDeltaY = end.y - start.y;
                double seDeltaZ = end.z - start.z;

                int steps = 200;

                while (steps-- >= 0) {

                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
                        return false;
                    }

                    if (currX == endX && currY == endY && currZ == endZ) {
                        return false;
                    }

                    boolean unboundedX = true;
                    boolean unboundedY = true;
                    boolean unboundedZ = true;

                    double stepX = 999;
                    double stepY = 999;
                    double stepZ = 999;
                    double deltaX = 999;
                    double deltaY = 999;
                    double deltaZ = 999;

                    if (endX > currX) {
                        stepX = currX + 1;
                    } else if (endX < currX) {
                        stepX = currX;
                    } else {
                        unboundedX = false;
                    }

                    if (endY > currY) {
                        stepY = currY + 1.0;
                    } else if (endY < currY) {
                        stepY = currY;
                    } else {
                        unboundedY = false;
                    }

                    if (endZ > currZ) {
                        stepZ = currZ + 1.0;
                    } else if (endZ < currZ) {
                        stepZ = currZ;
                    } else {
                        unboundedZ = false;
                    }

                    if (unboundedX) {
                        deltaX = (stepX - start.x) / seDeltaX;
                    }

                    if (unboundedY) {
                        deltaY = (stepY - start.y) / seDeltaY;
                    }

                    if (unboundedZ) {
                        deltaZ = (stepZ - start.z) / seDeltaZ;
                    }

                    if (deltaX == 0) {
                        deltaX = -1.0E-4;
                    }

                    if (deltaY == 0) {
                        deltaY = -1.0E-4;
                    }

                    if (deltaZ == 0) {
                        deltaZ = -1.0E-4;
                    }

                    EnumFacing facing;

                    if (deltaX < deltaY && deltaX < deltaZ) {
                        facing = endX > currX ? EnumFacing.WEST : EnumFacing.EAST;
                        start = new Vec3d(stepX, start.y + seDeltaY * deltaX, start.z + seDeltaZ * deltaX);
                    } else if (deltaY < deltaZ) {
                        facing = endY > currY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3d(start.x + seDeltaX * deltaY, stepY, start.z + seDeltaZ * deltaY);
                    } else {
                        facing = endZ > currZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3d(start.x + seDeltaX * deltaZ, start.y + seDeltaY * deltaZ, stepZ);
                    }

                    currX = MathHelper.floor(start.x) - (facing == EnumFacing.EAST ? 1 : 0);
                    currY = MathHelper.floor(start.y) - (facing == EnumFacing.UP ? 1 : 0);
                    currZ = MathHelper.floor(start.z) - (facing == EnumFacing.SOUTH ? 1 : 0);

                    blockPos = new BlockPos(currX, currY, currZ);
                    blockState = mc.world.getBlockState(blockPos);
                    block = blockState.getBlock();

                    if (block.canCollideCheck(blockState, false) && !blockDestruction) {
                        RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);

                        return collisionInterCheck != null;
                    }
                }
            }
        }

        return false;
    }

    public static void setAction(CPacketUseEntity packet, CPacketUseEntity.Action action) {
        ((AccessorCPacketUseEntity) packet).setAction(action);
    }

    public static void setEntityId(CPacketUseEntity packet, int entityId) {
        ((AccessorCPacketUseEntity) packet).setId(entityId);
    }

}
