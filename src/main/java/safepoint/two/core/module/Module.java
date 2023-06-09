package safepoint.two.core.module;

import safepoint.two.core.event.events.ModuleToggleEvent;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.KeySetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import safepoint.two.module.core.ClickGui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Module {
    public static Minecraft mc = Minecraft.getMinecraft();
    public List<Setting> settings = new ArrayList<>();
    public String name = getModuleInfo().name();
    public String description = getModuleInfo().description();
    public Category category = getModuleInfo().category();
    public KeySetting keyBind = new KeySetting("Keybind", Keyboard.KEY_NONE, this);
    public BooleanSetting enabled = new BooleanSetting("Enabled", false, this);
    public boolean isOpened = false;
    public boolean moduleDisableFlag = false;

    public Module() {
        initializeModule();
    }

    public void initializeModule() {
    }

    public void onEnable() {
    }

    public void onDisable() {
         this.moduleDisableFlag = true;
    }

    public void onThread(){
    }

    public void onTick(){
    }

    public void onWorldRender(){
    }

    public void doNullCheck(){
        if(mc.world == null || mc.player == null)
            return;
    }

    public void enableModule() {
        enabled.setValue(true);
        onEnable();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.post(new ModuleToggleEvent.Enable(this));
    }

    public void disableModule() {
        enabled.setValue(false);
        onDisable();
        MinecraftForge.EVENT_BUS.unregister(this);
        MinecraftForge.EVENT_BUS.post(new ModuleToggleEvent.Disable(this));
    }

    public void setEnabled(boolean enabled) {
        this.enabled.setValue(enabled);
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setKeyBind(int key) {
        keyBind.setValue(key);
    }

    public int getKeyBind() {
        return keyBind.getKey();
    }

    public String getKeyBindAsString() {
        return Keyboard.getKeyName(keyBind.getKey());
    }

    public Category getCategory() {
        return category;
    }

    public ModuleInfo getModuleInfo() {
        return getClass().getAnnotation(ModuleInfo.class);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public enum Category {
        Combat(new Color(213,100,100)),
        Core(new Color(182, 0, 33)),
        Misc(new Color(43,169,102)),
        Movement(new Color(61,29,158)),
        Player(new Color(222, 113, 0)),
        Visual(new Color(158,28,158));

        private final Color color;
        Category(Color color) {
            this.color = color;
        }

        public Color getColor(){
            if (ClickGui.getInstance().customColors.getValue())
                return color;
            else
                return ClickGui.getInstance().color.getColor();
        }
    }
}
