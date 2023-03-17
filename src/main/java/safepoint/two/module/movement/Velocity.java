package safepoint.two.module.movement;

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
        if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = event.getPacket();

            if (velocity.getEntityID() == mc.player.getEntityId())
                event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketExplosion) {
                event.setCanceled(true);
        }
    }

}
