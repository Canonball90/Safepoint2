package safepoint.two.core.event.events;

import safepoint.two.core.event.EventProcessor;

public class Render3DEvent extends EventProcessor {
    private final float partialTicks;

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}
