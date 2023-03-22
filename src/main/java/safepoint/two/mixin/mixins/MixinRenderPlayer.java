package safepoint.two.mixin.mixins;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.module.visual.Nametags;
import safepoint.two.module.visual.HandChams;

@Mixin(value = {RenderPlayer.class})
public abstract class MixinRenderPlayer {

    private float renderPitch, renderYaw, renderHeadYaw, prevRenderHeadYaw, prevRenderPitch, prevRenderYawOffset, prevPrevRenderYawOffset;

    @Inject(method = {"renderEntityName*"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo bigBlackMonke) {
        if (Nametags.getInstance().isEnabled()) {
            bigBlackMonke.cancel();
        }
    }

    @Redirect(method = "renderRightArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFF)V"))
    public void renderRightArmHook(float colorRed, float colorGreen, float colorBlue) {
        if (HandChams.getInstance().isEnabled() && HandChams.getInstance().mode.getValue()=="Glow") {
            GlStateManager.color(1.0f, 1.0f, 1.0f, HandChams.getInstance().color.getValue().getAlpha() / 255.0f);
        }
    }

    @Redirect(method = "renderLeftArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFF)V"))
    public void renderLeftArmHook(float colorRed, float colorGreen, float colorBlue) {
        if (HandChams.getInstance().isEnabled() && HandChams.getInstance().mode.getValue()=="Glow") {
            GlStateManager.color(1.0f, 1.0f, 1.0f, HandChams.getInstance().color.getValue().getAlpha() / 255.0f);
        }
    }
}
