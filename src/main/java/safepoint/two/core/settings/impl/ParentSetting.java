package safepoint.two.core.settings.impl;

import safepoint.two.core.module.Module;
import safepoint.two.core.settings.Setting;

import java.util.function.Predicate;

public class ParentSetting extends Setting<Boolean> {
    public boolean isOpen;
    public ParentSetting(String name, Boolean value, Module module) {
        super(name, value, module);
    }

    public ParentSetting(String name, boolean value, Module module, Predicate<Boolean> shown) {
        super(name, value, module, shown);
    }

    public Boolean getValue() {
        return value;
    }

    public void toggleValue() {
        value = !value;
    }
}
