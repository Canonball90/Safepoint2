package safepoint.two.utils.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import safepoint.two.Safepoint;
import safepoint.two.core.initializers.FriendInitializer;
import safepoint.two.utils.core.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static safepoint.two.Safepoint.mc;

public class PlayerUtil {
    public static EntityPlayer getClosestPlayer(float maxRange) {
        List<EntityPlayer> closestPlayer = (List<EntityPlayer>) mc.world.playerEntities.stream().filter(player -> player.getDistance(mc.player) < maxRange && !Safepoint.friendInitializer.isFriend(player.getName()) && player != mc.player && player.isEntityAlive()).collect(Collectors.toList());
        closestPlayer.sort(Comparator.comparingDouble(player -> player.getDistanceSq(mc.player)));
        if(!closestPlayer.isEmpty()) {
            return closestPlayer.get(0);
        }else{
            return null;
        }
    }

    public static EntityPlayer entityPlayer(final float range) {
        final TreeMap<Float, EntityPlayer> map = new TreeMap<Float, EntityPlayer>();
        final float[] distance = new float[1];
        final TreeMap<Float, EntityPlayer> treeMap = null;
        mc.world.playerEntities.stream().filter(e -> !e.equals((Object)mc.player) && !e.isDead).forEach(entityPlayer -> {
            distance[0] = entityPlayer.getDistance((Entity)mc.player);
            if (distance[0] < range && !Safepoint.friendInitializer.isFriend(entityPlayer.getName())) {
                treeMap.put(distance[0], entityPlayer);
            }
            return;
        });
        if (!map.isEmpty()) {
            return map.firstEntry().getValue();
        }
        return null;
    }

    public static EntityPlayer getTarget(final float range) {
        EntityPlayer currentTarget = null;
        for (int size = mc.world.playerEntities.size(), i = 0; i < size; ++i) {
            final EntityPlayer player = mc.world.playerEntities.get(i);
            if (!isntValid(player, range)) {
                if (currentTarget == null) {
                    currentTarget = player;
                } else if (mc.player.getDistanceSq(player) < mc.player.getDistanceSq(currentTarget)) {
                    currentTarget = player;
                }
            }
        }
        return currentTarget;
    }

    public static boolean isntValid(Entity entity, double range) {
        return entity == null || isDead(entity) || entity.equals(mc.player) || entity instanceof EntityPlayer && Safepoint.friendInitializer.isFriend(entity.getName()) || mc.player.getDistanceSq(entity) > MathUtil.square(range);
    }

    public static boolean isAlive(Entity entity) {
        return isLiving(entity) && !entity.isDead && ((EntityLivingBase) entity).getHealth() >= 0.0f;
    }

    public static boolean isDead(Entity entity) {
        return !isAlive(entity);
    }

    public static void changeSlot(int slot) {
        if (slot == -1) {return;}
        mc.player.inventory.currentItem = slot;
    }

    public static BlockPos getPosFloored(EntityPlayer p){
        return new BlockPos(Math.floor(p.posX), Math.floor(p.posY), Math.floor(p.posZ));
    }

    public static int getSlot(Item items) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == items) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    public static boolean placeBlock(BlockPos pos, EnumHand hand, boolean packet) {
        EnumFacing side = getFirstFacing(pos);
        if (side == null) return false;
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        return true;
    }

    public static EnumFacing getFirstFacing(BlockPos pos) {
        for (EnumFacing facing : getPossibleSides(pos)) {
            return facing;
        }
        return null;
    }

    public static List<EnumFacing> getPossibleSides(BlockPos pos) {
        List<EnumFacing> facings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour) == null || mc.world.getBlockState(neighbour).getBlock() == null) return facings;
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                IBlockState blockState = mc.world.getBlockState(neighbour);
                if (!blockState.getMaterial().isReplaceable()) {
                    facings.add(side);
                }
            }
        }
        return facings;
    }


    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
        }
    }

    public static boolean isEntityPlayerLoaded = false;
    public static boolean isEntityMobLoaded = false;
    public static boolean isEntityAnimalLoaded = false;
    public static boolean isEntityCrystalLoaded = false;
    public static boolean isEntityProjectileLoaded = false;
    public static List<Entity> entitiesList1 = new ArrayList<>();
    public static boolean entitiesListFlag = false;

    public static List<Entity> entitiesList() {
        if (!entitiesListFlag && !(mc.world == null || mc.world.loadedEntityList == null || mc.world.loadedEntityList.size() == 0))
            entitiesList1 = new ArrayList<>(mc.world.loadedEntityList);

        return entitiesList1;
    }

    public static boolean isEntityMob(Entity entity) {
        return (entity instanceof EntityMob || entity instanceof EntityShulker || entity instanceof EntitySlime || entity instanceof EntityGhast);
    }

    public static boolean isEntityAnimal(Entity entity) {
        return (entity instanceof EntityAnimal || entity instanceof EntitySquid);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedEntityPos(Entity entity, double ticks) {
        return new Vec3d(entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * ticks), entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * ticks), entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * ticks));
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static boolean isPlayerInHole() {
        BlockPos blockPos = getLocalPlayerPosFloored();

        IBlockState blockState = mc.world.getBlockState(blockPos);

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]
                {blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
                validHorizontalBlocks++;
        }

        return validHorizontalBlocks >= 4;
    }

    public static BlockPos getLocalPlayerPosFloored() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static boolean isFakeLocalPlayer(Entity entity) {
        return entity != null && entity.getEntityId() == -100 && mc.player != entity;
    }

    public static boolean isPassive(Entity e) {
        if (e instanceof EntityWolf && ((EntityWolf) e).isAngry()) {
            return false;
        }
        if (e instanceof EntityAgeable || e instanceof EntityAmbientCreature || e instanceof EntitySquid) {
            return true;
        }
        return e instanceof EntityIronGolem && ((EntityIronGolem) e).getRevengeTarget() == null;
    }

    public static boolean isLiving(Entity e) {
        return e instanceof EntityLivingBase;
    }

    public static float[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirX = me.posX - px;
        double dirY = me.posY - py;
        double dirZ = me.posZ - pz;
        double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX /= len;
        dirY /= len;
        dirZ /= len;
        double pitch = Math.asin(dirY);
        double yaw = Math.atan2(dirZ, dirX);
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;
        yaw += 90f;
        return new float[]{(float) yaw, (float) pitch};
    }

    public static void runEntityCheck() {
        isEntityPlayerLoaded = false;
        isEntityMobLoaded = false;
        isEntityAnimalLoaded = false;
        isEntityCrystalLoaded = false;
        isEntityProjectileLoaded = false;
        for (Entity entity : entitiesList()) {
            entitiesListFlag = true;
            if (entity instanceof EntityPlayer && entity != mc.player) isEntityPlayerLoaded = true;
            if (entity instanceof EntityMob || entity instanceof EntitySlime || entity instanceof EntityGhast || entity instanceof EntityDragon) isEntityMobLoaded = true;
            if (isEntityAnimal(entity)) isEntityAnimalLoaded = true;
            if (entity instanceof EntityEnderCrystal) isEntityCrystalLoaded = true;
            if (entity instanceof IProjectile || entity instanceof EntityShulkerBullet || entity instanceof EntityFireball || entity instanceof EntityEnderEye) isEntityProjectileLoaded = true;
        }
        entitiesListFlag = false;
    }

    public static double calculateDistanceWithPartialTicks(double originalPos, double finalPos, float renderPartialTicks) {
        return finalPos + (originalPos - finalPos) * (double)renderPartialTicks;
    }

    public static Vec3d interpolateEntity(Entity entity, float renderPartialTicks) {
        return new Vec3d(calculateDistanceWithPartialTicks(entity.posX, entity.lastTickPosX, renderPartialTicks), calculateDistanceWithPartialTicks(entity.posY, entity.lastTickPosY, renderPartialTicks), calculateDistanceWithPartialTicks(entity.posZ, entity.lastTickPosZ, renderPartialTicks));
    }

    public static Vec3d interpolateEntityRender(Entity entity, float renderPartialTicks) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * renderPartialTicks - mc.getRenderManager().renderPosX, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * renderPartialTicks - mc.getRenderManager().renderPosY, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * renderPartialTicks - mc.getRenderManager().renderPosZ);
    }

    public static boolean isBurrowed(Entity entity) {
        BlockPos pos = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY + 0.2), Math.floor(entity.posZ));
        return mc.world.getBlockState(pos).getBlock() != Blocks.AIR && mc.world.getBlockState(pos).getBlock() != Blocks.WATER && mc.world.getBlockState(pos).getBlock() != Blocks.FLOWING_WATER && mc.world.getBlockState(pos).getBlock() != Blocks.LAVA && mc.world.getBlockState(pos).getBlock() != Blocks.FLOWING_LAVA;
    }

    public static boolean isPosPlaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).getMaterial().isReplaceable() && mc.world.checkNoEntityCollision(new AxisAlignedBB(pos), mc.player);
    }

    private static void centerPlayer(double x, double y, double z) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, true));
        mc.player.setPosition(x, y, z);
    }

    private static double getDst(Vec3d vec) {
        return mc.player.getPositionVector().distanceTo(vec);
    }

    public static void setCenter() {
        BlockPos centerPos = mc.player.getPosition();
        double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();

        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);

        if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() + 0.5;
        }
        if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() + 0.5;
        }
        centerPlayer(x, y, z);
    }

    public static Vec3d selfCenterPos() {
        BlockPos centerPos = mc.player.getPosition();
        double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();

        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);

        if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() + 0.5;
        }
        if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() + 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() - 0.5;
        }
        if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
            x = centerPos.getX() - 0.5;
            z = centerPos.getZ() + 0.5;
        }
        return new Vec3d(x, y, z);
    }

    public static boolean isEntityVisible(Entity entity) {
        return mc.player.canEntityBeSeen(entity);
    }

    public static void strafe(float speed) {
        if (!isMoving())
            return;
        double yaw = getDirection();
        mc.player.motionX = -Math.sin(yaw) * speed;
        mc.player.motionZ = Math.cos(yaw) * speed;

    }

    public static double getDirection() {
        float rotationYaw = mc.player.rotationYaw;
        if (mc.player.moveForward < 0.0F)
            rotationYaw += 180.0F;
        float forward = 1.0F;
        if (mc.player.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.player.moveForward > 0.0F) {
            forward = 0.5F;
        }
        if (mc.player.moveStrafing > 0.0F)
            rotationYaw -= 90.0F * forward;
        if (mc.player.moveStrafing < 0.0F)
            rotationYaw += 90.0F * forward;
        return Math.toRadians(rotationYaw);
    }

    public static boolean isMoving() {
        return (mc.player != null && mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f);
    }
}
