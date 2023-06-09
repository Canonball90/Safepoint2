package safepoint.two.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import safepoint.two.module.visual.HandChams;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow protected abstract void
    renderArmFirstPerson(float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_);

    @Shadow public abstract void
    renderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded);

    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void renderFireInFirstPersonHook(CallbackInfo info) {
        info.cancel();
    }


    @Redirect(
            method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderArmFirstPerson(FFLnet/minecraft/util/EnumHandSide;)V"))
    public void mrHook(ItemRenderer itemRenderer, float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_) {
        if (HandChams.getInstance().isEnabled()) {
            if (HandChams.getInstance().mode.getValue()=="Wireframe") {
                Color handColor = HandChams.getInstance().color.getColor();
                glPushMatrix();
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                glLineWidth(1.5f);
                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glColor4f(handColor.getRed() / 255.0f, handColor.getGreen() / 255.0f, handColor.getBlue() / 255.0f, handColor.getAlpha() / 255.0f);
                renderArmFirstPerson(p_187456_1_, p_187456_2_, p_187456_3_);
                glDisable(GL_BLEND);
                glEnable(GL_TEXTURE_2D);
                glPopAttrib();
                glPopMatrix();
            } else {
                Color handColor = HandChams.getInstance().color.getColor();
                glPushMatrix();
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                glColor4f(handColor.getRed() / 255.0f, handColor.getGreen() / 255.0f, handColor.getBlue() / 255.0f, handColor.getAlpha() / 255.0f);
                renderArmFirstPerson(p_187456_1_, p_187456_2_, p_187456_3_);
                glDisable(GL_BLEND);
                glEnable(GL_TEXTURE_2D);
                glPopAttrib();
                glPopMatrix();
            }
        } else {
            renderArmFirstPerson(p_187456_1_, p_187456_2_, p_187456_3_);
        }
    }



    private void renderEffect(float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_) {
        float f = (float)Minecraft.getMinecraft().player.ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks();
        //Minecraft.getMinecraft().getTextureManager().bindTexture(RESOURCE);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        float f1 = 0.5F;
        GlStateManager.color(0.5F, 0.5F, 0.5F, 0.5f);

        for (int i = 0; i < 2; ++i)
        {
            GlStateManager.disableLighting();
            float f2 = 0.76F;
            GlStateManager.color(0.38F, 0.19F, 0.608F, 0.5f);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float f3 = 0.33333334F;
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            RenderPlayer renderPlayer = (RenderPlayer)Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(Minecraft.getMinecraft().player);
            if (renderPlayer != null) {
                if (p_187456_3_ == EnumHandSide.RIGHT) {
                    renderPlayer.renderRightArm(Minecraft.getMinecraft().player);
                } else {
                    renderPlayer.renderLeftArm(Minecraft.getMinecraft().player);
                }
            }
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
    }
}
