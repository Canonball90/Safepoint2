package safepoint.two.core.event.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import safepoint.two.core.event.EventProcessor;

@Cancelable
public class RenderRotationsEvent extends EventProcessor {

    private float yaw, pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float in) {
        yaw = in;
    }

    public void setPitch(float in) {
        pitch = in;
    }
}
