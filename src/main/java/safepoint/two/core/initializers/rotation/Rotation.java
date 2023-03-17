package safepoint.two.core.initializers.rotation;

import net.minecraft.network.play.client.CPacketPlayer;

import static safepoint.two.Safepoint.mc;

public class Rotation {
    RotationType type;
    public RotationPriority priority;
    float yaw, pitch;

    public Rotation(float yaw, float pitch, RotationType type, RotationPriority priority) {
        this.type = type;
        this.yaw = yaw;
        this.pitch = pitch;
        this.priority = priority;
    }

    public void doRotate() {
        if(type == RotationType.LEGIT) {
            mc.player.rotationYawHead = yaw;
            mc.player.rotationPitch = pitch;
        } else if(type == RotationType.PACKET) {
            mc.getConnection().sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
        }
    }
}
