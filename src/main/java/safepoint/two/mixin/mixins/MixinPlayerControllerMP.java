package safepoint.two.mixin.mixins;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import safepoint.two.core.event.events.EventPlayerDamageBlock;
import safepoint.two.core.event.events.PlayerAttackEvent;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        final EventPlayerDamageBlock event = new EventPlayerDamageBlock(posBlock, directionFacing);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled())
            cir.setReturnValue(false);
    }


    @Inject(method = "attackEntity", at = @At(value = "HEAD"), cancellable = true)
    public void attackEntityHook(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity != null) {
            PlayerAttackEvent event = new PlayerAttackEvent(targetEntity);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled())
                ci.cancel();
        }
    }

}
