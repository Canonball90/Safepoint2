package safepoint.two.core.initializers;

import net.minecraft.client.Minecraft;

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
