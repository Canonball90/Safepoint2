package safepoint.two.utils.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScaffoldBlock
        extends BlockPos {
    private long startTime = System.currentTimeMillis();

    public ScaffoldBlock(Vec3d vec) {
        super(vec);
    }

    public long getStartTime() {
        return this.startTime;
    }
}
