package safepoint.two.module.misc;

import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.utils.math.Inhibitator;

@ModuleInfo(name = "Timer", category = Module.Category.Misc, description = "")
public class Timer extends Module {

    DoubleSetting speed = new DoubleSetting("Speed", 4.0, 1.0, 10.0, this);
    BooleanSetting slowed = new BooleanSetting("Slowed", false, this);
    BooleanSetting pulse = new BooleanSetting("Pulse", false, this);
    DoubleSetting startVal = new DoubleSetting("StartPulse", 1.0, 1.0, 10.0, this,v -> pulse.getValue());
    DoubleSetting endVal = new DoubleSetting("EndPulse", 1.0, 1.0, 10.0, this,v -> pulse.getValue());
    DoubleSetting pulseSpeed = new DoubleSetting("Speed", 1.0, 0.0, 20.0, this,v -> pulse.getValue());
    Inhibitator inhibitator = new Inhibitator();
    safepoint.two.utils.math.Timer tickTimer = new safepoint.two.utils.math.Timer();

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

        float tttt = (float) (speed.getValue().doubleValue() - ((slowed.getValue()) ? tickTimer.getPassedTimeS() : 0f));

        if(tickTimer.passedS(10)){
            tickTimer.reset();
            disableModule();
        }

        mc.timer.tickLength = 50.0f / tttt;
        super.onTick();
    }
}
