package safepoint.two.module.misc;

import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.FloatSetting;

@ModuleInfo(name = "Timer", category = Module.Category.Misc, description = "")
public class Timer extends Module {

    FloatSetting speed = new FloatSetting("Speed", 4.0f, 1.0f, 10.0f, this);

    @Override
    public void onDisable() {
        mc.timer.tickLength = 50.0f;
        super.onDisable();
    }

    @Override
    public void onTick() {
        mc.timer.tickLength = 50.0f / speed.getValue();
        super.onTick();
    }
}
