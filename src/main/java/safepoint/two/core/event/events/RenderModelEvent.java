package safepoint.two.core.event.events;

import safepoint.two.core.event.EventProcessor;

public class RenderModelEvent extends EventProcessor {
    public boolean rotating = false;
    public float pitch = 0;

    public RenderModelEvent(){
        super();
    }
}
