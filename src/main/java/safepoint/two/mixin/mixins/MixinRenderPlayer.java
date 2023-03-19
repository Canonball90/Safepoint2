package safepoint.two.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.core.event.events.RenderRotationsEvent;
import safepoint.two.module.visual.Nametags;

@Mixin(value = {RenderPlayer.class})
public class MixinRenderPlayer {

    private float renderPitch, renderYaw, renderHeadYaw, prevRenderHeadYaw, prevRenderPitch, prevRenderYawOffset, prevPrevRenderYawOffset;

    @Inject(method = {"renderEntityName*"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo bigBlackMonke) {
        if (Nametags.getInstance().isEnabled()) {
            bigBlackMonke.cancel();
        }
    }
}
