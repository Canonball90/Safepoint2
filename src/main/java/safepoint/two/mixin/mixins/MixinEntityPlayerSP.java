package safepoint.two.mixin.mixins;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.RotationEvent;
import safepoint.two.core.event.events.RotationUpdateEvent;
import safepoint.two.core.event.events.UpdateWalkingPlayerEvent;
import safepoint.two.core.initializers.RotationInitializer;

import static safepoint.two.Safepoint.mc;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "onUpdateWalkingPlayer",at = @At("RETURN"))
    private void onUpdateWalkingPlayer(CallbackInfo ci){
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent();
        MinecraftForge.EVENT_BUS.post(event);

        RotationEvent eventR = new RotationEvent();
        MinecraftForge.EVENT_BUS.post(eventR);

        if (eventR.isCanceled()) {
            ci.cancel();
        }

    }

}
