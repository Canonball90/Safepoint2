package safepoint.two.mixin.mixins;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.core.event.events.RenderModelEvent;

import static safepoint.two.Safepoint.mc;

@Mixin(ModelBiped.class)
public class MixinModelBiped {

    @Shadow
    public ModelRenderer bipedRightArm;

    public int heldItemRight;

    @Shadow
    public ModelRenderer bipedHead;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        if(heldItemRight == 3)
            this.bipedRightArm.rotateAngleY = 0F;

        if (p_setRotationAngles_7_ instanceof EntityPlayer && p_setRotationAngles_7_.equals(mc.player)) {
            RenderModelEvent event = new RenderModelEvent();
            MinecraftForge.EVENT_BUS.post(event);
            if(event.rotating)bipedHead.rotateAngleX = event.pitch / (180F / (float) Math.PI);
        }
    }

}
