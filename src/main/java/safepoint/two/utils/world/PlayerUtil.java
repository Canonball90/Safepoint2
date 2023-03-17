package safepoint.two.utils.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

    public static boolean isLiving(Entity entity) {
        return entity instanceof EntityLivingBase;
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
}
