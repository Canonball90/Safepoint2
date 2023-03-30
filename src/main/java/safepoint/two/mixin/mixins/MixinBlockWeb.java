package safepoint.two.mixin.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import safepoint.two.Safepoint;
import safepoint.two.module.movement.AntiWeb;

@Mixin(value = {BlockWeb.class})
public class MixinBlockWeb extends Block {
    protected MixinBlockWeb() {
        super(Material.WEB);
    }


    @Inject(method = {"getCollisionBoundingBox"}, at = {@At("HEAD")}, cancellable = true)
    public void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (Safepoint.moduleInitializer.find(AntiWeb.class).isEnabled() && AntiWeb.INSTANCE.mode.getValue().equalsIgnoreCase("Solid")) {
            cir.setReturnValue(FULL_BLOCK_AABB);
        }
    }
}
