package safepoint.two.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;

@ModuleInfo(name = "Velocity", description = "", category = Module.Category.Movement)
public class Velocity extends Module {

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.Receive event){
        Entity entity;
        SPacketEntityStatus packet;
        if (event.getPacket() instanceof SPacketEntityVelocity && ((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
            event.setCanceled(true);
            return;
        }
        if (event.getPacket() instanceof SPacketEntityStatus && (packet = event.getPacket()).getOpCode() == 31 && (entity = packet.getEntity(mc.world)) instanceof EntityFishHook) {
            EntityFishHook fishHook = (EntityFishHook) entity;
            if (fishHook.caughtEntity == mc.player) {
                event.setCanceled(true);
            }
        }
        if (event.getPacket() instanceof SPacketExplosion) {
            event.setCanceled(true);
        }
    }

}
