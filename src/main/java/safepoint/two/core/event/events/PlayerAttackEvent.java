package safepoint.two.core.event.events;

import net.minecraft.entity.Entity;
import safepoint.two.core.event.EventProcessor;

public class PlayerAttackEvent extends EventProcessor {
    public Entity target;

    public PlayerAttackEvent(Entity target) {
        this.target = target;
    }
}
