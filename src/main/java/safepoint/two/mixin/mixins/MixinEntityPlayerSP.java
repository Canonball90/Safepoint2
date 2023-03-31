package safepoint.two.mixin.mixins;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.EventMove;
import safepoint.two.core.event.events.RotationEvent;
import safepoint.two.core.event.events.RotationUpdateEvent;
import safepoint.two.core.event.events.UpdateWalkingPlayerEvent;
import safepoint.two.core.initializers.RotationInitializer;

import static safepoint.two.Safepoint.mc;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Shadow public abstract void move(MoverType type, double x, double y, double z);

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

    @Inject(method = { "move" }, at = { @At("HEAD") }, cancellable = true)
    protected void move(final MoverType type, final double x, final double y, final double z, final CallbackInfo ci) {
        EventMove event = new EventMove(type, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.get_x() != x || event.get_y() != y ||event.get_z() != z){
            move(type, event.get_x(), event.get_y(), event.get_z());
            ci.cancel();
        }
    }

}
