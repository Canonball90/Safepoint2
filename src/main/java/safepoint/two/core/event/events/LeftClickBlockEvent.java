package safepoint.two.core.event.events;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import safepoint.two.core.event.EventProcessor;

public class LeftClickBlockEvent extends EventProcessor {
    public BlockPos blockPos;
    public EnumFacing face;

    public LeftClickBlockEvent(BlockPos blockPos, EnumFacing face) {
        this.blockPos = blockPos;
        this.face = face;
    }
}
