package safepoint.two.core.settings.impl;

import safepoint.two.core.module.Module;
import safepoint.two.core.settings.Setting;

import java.util.function.Predicate;

public class KeySetting extends Setting<Integer> {

    public boolean isTyping = false;

    public KeySetting(String name, int value, Module module) {
        super(name, value, module);
    }

    public KeySetting(String name, int value, Module module, Predicate<Integer> shown) {
        super(name, value, module, shown);
    }

    public int getKey() {
        return value;
    }

    public void setBind(int bind){
        value = bind;
    }

    public KeySetting setParent(ParentSetting parentSetting){
        this.parentSetting = parentSetting;
        hasParentSetting = true;

        return this;
    }
}
