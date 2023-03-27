package safepoint.two.module.core;

import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.ColorSetting;
import safepoint.two.core.settings.impl.IntegerSetting;
import org.lwjgl.input.Keyboard;

import java.awt.*;

@ModuleInfo(name = "Click Gui", category = Module.Category.Core, description = "Displays the clickgui.")
public class ClickGui extends Module {
    static ClickGui INSTANCE = new ClickGui();
    public BooleanSetting customColors = new BooleanSetting("Cat Colors", false, this);
    public ColorSetting color = new ColorSetting("Color", new Color(239, 12, 12, 208), this, v -> !customColors.getValue());
    public ColorSetting backgroundColor = new ColorSetting("Background Color", new Color(0, 0, 0, 50), this);
    public IntegerSetting integerSetting = new IntegerSetting("I", 100, 0, 500, this);
    public BooleanSetting customFont = new BooleanSetting("Custom Font", true, this);
    public BooleanSetting shadow = new BooleanSetting("Shadow", true, this);

    @Override
    public void initializeModule() {
        setInstance();
        setKeyBind(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(safepoint.two.guis.clickgui.ClickGui.getInstance());
    }

    @Override
    public void onDisable() {
        Safepoint.configInitializer.save();
    }

    @Override
    public void onTick() {
        if (!(mc.currentScreen instanceof safepoint.two.guis.clickgui.ClickGui) && isEnabled())
            disableModule();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

}
