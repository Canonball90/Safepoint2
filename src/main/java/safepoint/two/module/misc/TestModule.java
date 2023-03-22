package safepoint.two.module.misc;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.*;

import java.awt.*;
import java.util.Arrays;

@ModuleInfo(name = "Test Module", category = Module.Category.Misc, description = "This is a test module")
public class TestModule extends Module {
    EntityOtherPlayerMP fake_player;

    BooleanSetting booleanSet = new BooleanSetting("Boolean", true, this);
    ColorSetting colorSet = new ColorSetting("Color", new Color(255,0,0), this);
    DoubleSetting doubleSet = new DoubleSetting("Double", 3.5d, 0.0d, 5.0d, this);
    EnumSetting enumSet = new EnumSetting("Enum", "Mode 1", Arrays.asList("Mode 1", "Mode 2", "Mode 3"), this);
    FloatSetting floatSet = new FloatSetting("Float", 1.3f, 0.0f, 2.0f, this);
    IntegerSetting intSet = new IntegerSetting("Integer", 5, 0, 10, this);
    StringSetting stringSet = new StringSetting("String", "String", this);

    ParentSetting parentSet = new ParentSetting("Parent", false, this);
    BooleanSetting ParentbooleanSet = new BooleanSetting("Boolean", true, this).setParent(parentSet);
    ColorSetting ParentcolorSet = new ColorSetting("Color", new Color(255,0,0), this).setParent(parentSet);
    DoubleSetting ParentdoubleSet = new DoubleSetting("Double", 3.5d, 0.0d, 5.0d, this).setParent(parentSet);
    EnumSetting ParentenumSet = new EnumSetting("Enum", "Mode 1", Arrays.asList("Mode 1", "Mode 2", "Mode 3"), this).setParent(parentSet);
    FloatSetting ParentfloatSet = new FloatSetting("Float", 1.3f, 0.0f, 2.0f, this).setParent(parentSet);
    IntegerSetting ParentintSet = new IntegerSetting("Integer", 5, 0, 10, this).setParent(parentSet);
    StringSetting ParentstringSet = new StringSetting("String", "String", this).setParent(parentSet);

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null)
            return;
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onDisable() {
    }
}
