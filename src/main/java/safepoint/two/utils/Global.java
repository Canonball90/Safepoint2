package safepoint.two.utils;

import net.minecraft.client.Minecraft;
import safepoint.two.Safepoint;

public interface Global {
    Minecraft mc = Minecraft.getMinecraft();

    default boolean nullCheck() {
        return mc.world == null || mc.player == null;
    }

    default Safepoint getClient() {
        return Safepoint.INSTANCE;
    }
}
