package safepoint.two.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.EventModelPlayerRender;
import safepoint.two.module.visual.Chams;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer {

    @Shadow
    ModelRenderer bipedLeftArmwear;
    @Shadow
    ModelRenderer bipedRightArmwear;
    @Shadow
    ModelRenderer bipedLeftLegwear;
    @Shadow
    ModelRenderer bipedRightLegwear;
    @Shadow
    ModelRenderer bipedBodyWear;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", remap = false))
    public void pushMatrix(){
        //GlStateManager.enableAlpha();
        //GlStateManager.enableBlend();
        //GlStateManager.color(1.f,0.11f,1f,0.2f); // TEST
        GL11.glPushMatrix();
    }

    @Inject(method = "renderCape", at = @At("HEAD"), cancellable = true)
    public void renderCape(float scale, CallbackInfo ci){
        // Overwrite with our color
        Chams chamsModule = (Chams) Safepoint.moduleInitializer.find(Chams.class);
        EntityPlayer e = chamsModule.lastPlayer;

        if(!chamsModule.isEnabled() || e == null) {
            GL11.glColor4f(1,1,1,1);
            return;
        }

        if(Safepoint.friendInitializer.isFriend(e.getName()) && chamsModule.friend.getValue() ||
                !Safepoint.friendInitializer.isFriend(e.getName()) && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfColor.getValue().getRed(),chamsModule.selfColor.getValue().getGreen(),chamsModule.selfColor.getValue().getBlue(),chamsModule.selfColor.getValue().getAlpha());
            } else if(Safepoint.friendInitializer.isFriend(e.getName())){
                //friend settings
                GL11.glColor4f(chamsModule.friendColor.getValue().getRed(),chamsModule.friendColor.getValue().getGreen(),chamsModule.friendColor.getValue().getBlue(),chamsModule.friendColor.getValue().getAlpha());
            } else if(!Safepoint.friendInitializer.isFriend(e.getName())){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyColor.getValue().getRed(),chamsModule.enemyColor.getValue().getGreen(),chamsModule.enemyColor.getValue().getBlue(),chamsModule.enemyColor.getValue().getAlpha());
            }
        }else{
            GL11.glColor4f(1,1,1,1);
        }
    }

    @Inject(method = "render", at = @At("RETURN"), cancellable = false)
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci){

    }

    @Shadow
    public void render(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
    }

    @Inject(method = { "render" }, at = { @At("HEAD") }, cancellable = true)
    private void renderPre(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale, final CallbackInfo ci) {
        final EventModelPlayerRender modelrenderpre = new EventModelPlayerRender((ModelBase)ModelPlayer.class.cast(this), entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0);
        MinecraftForge.EVENT_BUS.post((Event)modelrenderpre);
        if (modelrenderpre.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = { "render" }, at = { @At("RETURN") })
    private void renderPost(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale, final CallbackInfo ci) {
        final EventModelPlayerRender modelrenderpost = new EventModelPlayerRender((ModelBase)ModelPlayer.class.cast(this), entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 1);
        MinecraftForge.EVENT_BUS.post((Event)modelrenderpost);
    }
}

