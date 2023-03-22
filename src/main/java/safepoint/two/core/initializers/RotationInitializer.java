package safepoint.two.core.initializers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import safepoint.two.utils.world.RotationUtil;

public class RotationInitializer {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static float yaw;
    public static float pitch;
    public static boolean rotating;

    public static void setRotations(float yawAngle, float pitchAngle){
        yaw = yawAngle;
        pitch = pitchAngle;
        rotating = true;
    }
    public static void reset() {
        yaw = Minecraft.getMinecraft().player.rotationYaw;
        pitch = Minecraft.getMinecraft().player.rotationPitch;
        rotating = false;
    }

    //Todo
    public void rotate(Entity target, Boolean legit){
        float[] arrf = RotationUtil.getRotations(target);
        if(legit) {
            mc.player.rotationYaw = arrf[0];
            mc.player.rotationPitch = arrf[1];
        }
        mc.player.renderYawOffset = arrf[0];
        mc.player.rotationYawHead = arrf[1];
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(arrf[0], (float) MathHelper.normalizeAngle((int) arrf[1], 360), mc.player.onGround));
    }

    public static float getYaw() {
        return yaw;
    }

    public static float getPitch(){
        return pitch;
    }

    public static boolean isRotating(){
        return rotating;
    }
}
