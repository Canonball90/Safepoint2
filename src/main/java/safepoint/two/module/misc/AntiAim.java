package safepoint.two.module.misc;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.event.events.RenderModelEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.mixin.mixins.AccessorCPacketPlayer;
import safepoint.two.utils.math.Timer;

@ModuleInfo(name = "AntiAim", category = Module.Category.Misc, description = "Spawns fake entity")
public class AntiAim extends Module {

    FloatSetting speed = new FloatSetting("Speed", 2, 1, 10, this);
    transient private static boolean rotating = false;
    transient public static float yaw;
    transient public static float pitch;
    transient public static float renderPitch;
    transient public static boolean shouldSpoofPacket;
    public Timer time = new Timer();

    @Override
    public void onTick() {
        if(mc.player == null || mc.world == null) return;
        setYawAndPitch(mc.player.rotationYaw + time.getPassedTimeMs(), mc.player.rotationPitch, mc.player.rotationPitch);
        super.onTick();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event){
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet1 = (CPacketPlayer) event.getPacket();
            if (shouldSpoofPacket) {
                ((AccessorCPacketPlayer) packet1).setYaw(yaw);
                ((AccessorCPacketPlayer) packet1).setPitch(pitch);
                shouldSpoofPacket = false;
            }
        }
    }


    @SubscribeEvent
    public void renderModelRotation(RenderModelEvent event) {
        if (rotating) {
            event.rotating = true;
            event.pitch = renderPitch;
        }
    }

    public void setYawAndPitch(float yaw1, float pitch1, float renderPitch1) {
        yaw = yaw1;
        pitch = pitch1;
        renderPitch = renderPitch1;
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
        shouldSpoofPacket = true;
        rotating = true;
    }
}
