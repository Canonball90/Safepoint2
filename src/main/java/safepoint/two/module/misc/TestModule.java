package safepoint.two.module.misc;

import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.*;

import java.awt.*;
import java.util.Arrays;

//This registers information about the module (Name, category and description)
@ModuleInfo(name = "Test Module", category = Module.Category.Misc, description = "This is a test module")
public class TestModule extends Module {

    //Settings examples:
    BooleanSetting booleanSet = new BooleanSetting("Boolean", true, this); //Boolean setting
    ColorSetting colorSet = new ColorSetting("Color", new Color(255,0,0), this); //Color setting
    DoubleSetting doubleSet = new DoubleSetting("Double", 3.5d, 0.0d, 5.0d, this); //Double slider setting
    EnumSetting enumSet = new EnumSetting("Enum", "Mode 1", Arrays.asList("Mode 1", "Mode 2", "Mode 3"), this); //Enumeration/Mode setting
    FloatSetting floatSet = new FloatSetting("Float", 1.3f, 0.0f, 2.0f, this); //Float slider setting
    IntegerSetting intSet = new IntegerSetting("Integer", 5, 0, 10, this); //Integer slider setting
    StringSetting stringSet = new StringSetting("String", "String", this); //String/text box setting

    //Settings category:
    ParentSetting parentSet = new ParentSetting("Parent", false, this);
    //Settings part of the category (because of .setParent())
    BooleanSetting ParentBooleanSet = new BooleanSetting("Boolean", true, this).setParent(parentSet);
    ColorSetting ParentColorSet = new ColorSetting("Color", new Color(255,0,0), this).setParent(parentSet);
    DoubleSetting ParentDoubleSet = new DoubleSetting("Double", 3.5d, 0.0d, 5.0d, this).setParent(parentSet);
    EnumSetting ParentEnumSet = new EnumSetting("Enum", "Mode 1", Arrays.asList("Mode 1", "Mode 2", "Mode 3"), this).setParent(parentSet);
    FloatSetting ParentFloatSet = new FloatSetting("Float", 1.3f, 0.0f, 2.0f, this).setParent(parentSet);
    IntegerSetting ParentIntSet = new IntegerSetting("Integer", 5, 0, 10, this).setParent(parentSet);
    StringSetting ParentStringSet = new StringSetting("String", "String", this).setParent(parentSet);

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null) return; //This is a check to prevent crashes
        //The code inside this runs whenever your turn ON the module
        super.onEnable();
    }

    @Override
    public void onDisable() {
        //The code inside this runs whenever your turn OFF the module
        super.onDisable();
    }

    @Override
    public void onTick() {
        //The code inside this runs every tick when the module is activated
        super.onTick();
    }

    @Override
    public void onWorldRender() {
        //The code inside this runs every time the world is rendered, this is used for rendering things
        super.onWorldRender();
    }
}
