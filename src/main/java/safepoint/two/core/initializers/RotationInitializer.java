package safepoint.two.core.initializers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.core.Core;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.event.events.RenderModelEvent;
import safepoint.two.mixin.mixins.AccessorCPacketPlayer;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.world.RotationUtil;

public class RotationInitializer extends Core {

    public static Minecraft mc = Minecraft.getMinecraft();
    transient public static float yaw;
    transient public static float pitch;
    transient public static float renderPitch;
    transient public static boolean shouldSpoofPacket;
    public static float newYaw;
    transient public static float prevYaw;
    transient public static float prevPitch;
    public static float theInt = 0;
    public static boolean flag = false;
    private static final Timer rotateTimer = new Timer();

    public RotationInitializer() {
        super("RotationInitializer");
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new RotationUtil());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (shouldSpoofPacket) {
                ((AccessorCPacketPlayer) packet).setYaw(yaw);
                ((AccessorCPacketPlayer) packet).setPitch(pitch);
            }
        }
    }

    @SubscribeEvent
    public void renderModelRotation(RenderModelEvent event) {
        if (shouldSpoofPacket) {
            event.rotating = true;
            event.pitch = renderPitch;
        }
    }

    public static float[] getRotations(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((difX * difX + difZ * difZ));
        return new float[]{(float)MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)), (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }

    public static float[] getRotationsBlock(BlockPos block, EnumFacing face, boolean Legit) {
        double x = block.getX() + 0.5 - mc.player.posX +  (double) face.getXOffset()/2;
        double z = block.getZ() + 0.5 - mc.player.posZ +  (double) face.getZOffset()/2;
        double y = (block.getY() + 0.5);

        if (Legit)
            y += 0.5;

        double d1 = mc.player.posY + mc.player.getEyeHeight() - y;
        double d3 = MathHelper.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (Math.atan2(d1, d3) * 180.0D / Math.PI);

        if (yaw < 0.0F) {
            yaw += 360f;
        }
        return new float[]{yaw, pitch};
    }

    public static float calcNormalizedAngleDiff(float angle1, float angle2) {
        float angleDiff1 = Math.abs(angle1 - angle2);
        if (angleDiff1 < 180) return angleDiff1;
        else return 360.0f - Math.max(angle1, angle2) + Math.min(angle1, angle2);
    }

    private static int calcYawDelta(float prevYaw, float newYaw) {
        if (newYaw - prevYaw >= 180 || (newYaw - prevYaw < 0 && newYaw - prevYaw >= -180)) return -1;
        else if (newYaw - prevYaw < -180 || (newYaw - prevYaw >= 0 && newYaw - prevYaw < 180)) return 1;
        return -69;
    }

    public static float normalizeAngle(float angle) {
        float angle2 = angle;
        if (angle2 < 0.0f) angle2 += 360.0f;
        angle2 %= 360.0f;

        if (angle2 > 360.0f)
            angle2 = 360.0f;

        if (angle2 < 0.0)
            angle2 = 0.0f;

        return angle2;
    }

    public static void resetRotation(boolean slowRotate, float degreesPerSecond) {
        if (shouldSpoofPacket) {
            if (slowRotate) {

                float normalizedOriginalYaw = normalizeAngle(mc.player.rotationYawHead);

                if (normalizedOriginalYaw == 0.0f || normalizedOriginalYaw == 360.0f)
                    normalizedOriginalYaw = 1.0f;

                if (!(newYaw >= normalizedOriginalYaw - 2 && newYaw <= normalizedOriginalYaw + 2) && rotateTimer.passedMs(1)) {
                    newYaw += (degreesPerSecond / 1000.0f) * calcYawDelta(newYaw, normalizedOriginalYaw);
                    newYaw = normalizeAngle(newYaw);
                    rotateTimer.reset();
                }
                else if (newYaw >= normalizedOriginalYaw - 2 && newYaw <= normalizedOriginalYaw + 2) {
                    shouldSpoofPacket = false;
                    flag = false;
                }

                setYawAndPitch(newYaw, mc.player.rotationPitch, mc.player.rotationPitch);

            }
            else {
                yaw = mc.player.rotationYawHead;
                pitch = mc.player.rotationPitch;
                flag = false;
                shouldSpoofPacket = false;
            }
        }
    }

    public static void setYawAndPitch(float yaw1, float pitch1, float renderPitch1) {
        yaw = yaw1;
        pitch = pitch1;
        renderPitch = renderPitch1;
        mc.player.renderYawOffset = yaw1;
    }

    public static void lookAtTarget(Entity entity, boolean slowRotate, float degreesPerSecond) {
        float[] v = getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());
        float[] v2 = getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector().add(0, -0.5, 0));

        shouldSpoofPacket = true;

        if (slowRotate) {
            float normalizedYaw = normalizeAngle(v[0]);

            if (normalizedYaw == 0.0f || normalizedYaw == 360.0f)
                normalizedYaw = 1.0f;

            if (!flag) {
                newYaw = normalizeAngle(mc.player.rotationYawHead);
                flag = true;
            }

            if (!(newYaw >= normalizedYaw - 1 && newYaw <= normalizedYaw + 1) && rotateTimer.passedMs(1)) {
                newYaw += (degreesPerSecond / 1000.0f) * calcYawDelta(newYaw, normalizedYaw);
                newYaw = normalizeAngle(newYaw);
                rotateTimer.reset();
            }

            setYawAndPitch(newYaw, v[1], v2[1]);
        }
        else setYawAndPitch(v[0], v[1], v2[1]);
    }

    public static void lookAtVec3d(Vec3d vec3d) {
        float[] v = getRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec3d);

        setYawAndPitchBlock(v[0], v[1]);
    }

    public static void resetRotationBlock() {
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(prevYaw, prevPitch, mc.player.onGround));
    }

    public static void setYawAndPitchBlock(float yaw1, float pitch1) {
        prevYaw = mc.player.rotationYaw;
        prevPitch = mc.player.rotationPitch;
        mc.player.renderYawOffset = yaw1;
        renderPitch = pitch1;
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw1, pitch1, mc.player.onGround));
    }

}
