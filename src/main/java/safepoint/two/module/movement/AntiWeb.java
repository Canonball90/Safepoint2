package safepoint.two.module.movement;

import net.minecraft.entity.Entity;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.utils.core.MappingUtil;

import java.lang.reflect.Field;
import java.util.Arrays;

@ModuleInfo(name = "AntiWeb", category = Module.Category.Movement, description = "")
public class AntiWeb extends Module {

    public EnumSetting mode = new EnumSetting("Mode","Solid", Arrays.asList("Solid", "Normal"), this);
    public static AntiWeb INSTANCE;

    public AntiWeb(){
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if(mode.getValue().equalsIgnoreCase("Normal")) {
            try {
                Field isInWeb = Entity.class.getDeclaredField(MappingUtil.isInWeb);
                isInWeb.setAccessible(true);
                isInWeb.setBoolean(mc.player, false);
            } catch (Exception ex) {
                this.disableModule();
            }
        }
    }
}
