package safepoint.two.core.event.events;

import safepoint.two.core.event.EventProcessor;

public class RotationEvent extends EventProcessor {
    float yaw;
    float pitch;

    public RotationEvent() {}

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}

