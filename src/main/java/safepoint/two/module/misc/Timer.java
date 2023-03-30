package safepoint.two.module.misc;

import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.utils.math.Inhibitator;

@ModuleInfo(name = "Timer", category = Module.Category.Misc, description = "")
public class Timer extends Module {

    DoubleSetting speed = new DoubleSetting("Speed", 4.0, 1.0, 10.0, this);
    BooleanSetting pulse = new BooleanSetting("Pulse", false, this);
    DoubleSetting startVal = new DoubleSetting("StartPulse", 1.0, 1.0, 10.0, this,v -> pulse.getValue());
    DoubleSetting endVal = new DoubleSetting("EndPulse", 1.0, 1.0, 10.0, this,v -> pulse.getValue());
    DoubleSetting pulseSpeed = new DoubleSetting("Speed", 1.0, 0.0, 10.0, this,v -> pulse.getValue());
    Inhibitator inhibitator = new Inhibitator();

    @Override
    public void onDisable() {
        mc.timer.tickLength = 50.0f;
        super.onDisable();
    }

    @Override
    public void onTick() {
        if(pulse.getValue()){
            inhibitator.doInhibitation(speed, pulseSpeed.getValue(), startVal.getValue(), endVal.getValue());
        }
        mc.timer.tickLength = 50.0f / speed.getValue().floatValue();
        super.onTick();
    }
}
