package safepoint.two.core.event.events;

import net.minecraft.entity.Entity;
import safepoint.two.core.event.EventProcessor;

public class CrystalAttackEvent extends EventProcessor {

    int entityId;
    Entity entity;

    public CrystalAttackEvent(int entityId, Entity entity) {
        this.entityId = entityId;
        this.entity = entity;
    }

    public int getEntityId(){
        return entityId;
    }

    public Entity getEntity(){
        return entity;
    }
}