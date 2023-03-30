package safepoint.two.module.movement;

import net.minecraft.entity.Entity;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.utils.core.MappingUtil;

import java.lang.reflect.Field;

@ModuleInfo(name = "AntiWeb", category = Module.Category.Movement, description = "")
public class AntiWeb extends Module {

    @Override
    public void onTick() {
        try {
            Field isInWeb = Entity.class.getDeclaredField(MappingUtil.isInWeb);
            isInWeb.setAccessible(true);
            isInWeb.setBoolean(mc.player, false);
        } catch (Exception ex) {
            this.disableModule();
        }
    }
}
