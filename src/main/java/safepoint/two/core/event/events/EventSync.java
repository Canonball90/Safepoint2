package safepoint.two.core.event.events;

import safepoint.two.core.event.EventProcessor;

public class EventSync extends EventProcessor {

    float yaw;
    float pitch;
    boolean onGround;

    public EventSync(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isOnGround() {return onGround;}
}
