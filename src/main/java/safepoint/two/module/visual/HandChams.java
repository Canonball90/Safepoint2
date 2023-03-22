package safepoint.two.module.visual;

import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.ColorSetting;
import safepoint.two.core.settings.impl.EnumSetting;

import java.awt.*;
import java.util.Arrays;

@ModuleInfo(name = "HandChams", category = Module.Category.Visual, description = "Hand esp lol")
public class HandChams extends Module {
    public EnumSetting mode = new EnumSetting("Mode", "Glow", Arrays.asList("Glow", "Wireframe"), this);
    public ColorSetting color = new ColorSetting("Color", new Color(255,0,0, 50), this);
    public static HandChams INSTANCE;

    public HandChams() {
        INSTANCE = this;
    }

    public static HandChams getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HandChams();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onTick() {
        super.onTick();
    }
}
