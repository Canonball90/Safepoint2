package safepoint.two.core.event;

import net.minecraft.client.renderer.GlStateManager;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.DeathEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import safepoint.two.core.event.events.Render3DEvent;

import static safepoint.two.Safepoint.mc;

public class EventListener {

    public void init(boolean load) {
        if (load)
            MinecraftForge.EVENT_BUS.register(this);
        else MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.RenderTickEvent event) {
        Safepoint.moduleInitializer.onTick();
        if (mc.world == null || mc.player == null)
            return;
        mc.world.playerEntities.stream().filter(player -> player != null && !(player.getHealth() > 0.0f)).map(DeathEvent::new).forEach(MinecraftForge.EVENT_BUS::post);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState())
            Safepoint.moduleInitializer.onKey(Keyboard.getEventKey());
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            mc.profiler.startSection("Safepoint");
            return;
        }
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0f);
        Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        Safepoint.moduleInitializer.onWorldRender();
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        mc.profiler.endSection();
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (!Safepoint.hudComponentInitializer.getHudModules().isEmpty())
            Safepoint.hudComponentInitializer.drawText();
    }
}
