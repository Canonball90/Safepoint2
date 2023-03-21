package safepoint.two.core.module;

import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.ClickGui;
import org.lwjgl.input.Keyboard;
import safepoint.two.module.combat.Aura;
import safepoint.two.module.combat.AutoCrystal;
import safepoint.two.module.combat.Criticals;
import safepoint.two.module.combat.Surround;
import safepoint.two.module.core.ChatNotifications;
import safepoint.two.module.core.ConfigSave;
import safepoint.two.module.core.HudEditor;
import safepoint.two.module.core.MusicPlayer;
import safepoint.two.module.misc.FakePlayer;
import safepoint.two.module.misc.TestModule;
import safepoint.two.module.movement.Sprint;
import safepoint.two.module.movement.Velocity;
import safepoint.two.module.player.AutoToolModule;
import safepoint.two.module.player.PacketEXP;
import safepoint.two.module.player.VClip;
import safepoint.two.module.visual.Chams;
import safepoint.two.module.visual.HoleESP;
import safepoint.two.module.visual.Nametags;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleInitializer {

    List<Module> moduleList;

    public ModuleInitializer(){
        moduleList = new ArrayList<>();
        init();
    }

    public void init(){
        //COMBAT
        moduleList.add(new Aura());
        moduleList.add(new AutoCrystal());
        moduleList.add(new Criticals());
        moduleList.add(new Surround());

        //CORE
        moduleList.add(new ChatNotifications());
        moduleList.add(new safepoint.two.module.core.ClickGui());
        moduleList.add(new ConfigSave());
        moduleList.add(new HudEditor());
        moduleList.add(new MusicPlayer());

        //MISC
        moduleList.add(new FakePlayer());
        moduleList.add(new TestModule());

        //MOVEMENT
        moduleList.add(new Sprint());
        moduleList.add(new Velocity());

        //PLAYER
        moduleList.add(new AutoToolModule());
        moduleList.add(new PacketEXP());
        moduleList.add(new VClip());

        //VISUAL
        moduleList.add(new Chams());
        moduleList.add(new HoleESP());
        moduleList.add(new Nametags());
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public List<Module> getModuleList() {
        return moduleList;
    }

    public List<Module> getCategoryModules(Module.Category category) {
        final List<Module> list = new ArrayList<>();
        for (Module module : moduleList) {
            if (module.getCategory() == category)
                list.add(module);
        }

        return list;
    }

    public void addModules(String folder) {
        try {
            final List<Class<?>> classes = ClassFinder.from("safepoint.two.module." + folder);
            if (classes == null)
                return;
            for (Class<?> clazz : classes) {
                if (!Modifier.isAbstract(clazz.getModifiers()) && Module.class.isAssignableFrom(clazz)) {
                    for (Constructor<?> constructor : clazz.getConstructors()) {
                        final Module instance = (Module) constructor.newInstance();
                        for (Field field : instance.getClass().getDeclaredFields())
                            if (!field.isAccessible())
                                field.setAccessible(true);

                        moduleList.add(instance);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTick(){
        for(Module module : moduleList){
            if(module.isEnabled())
                module.onTick();
        }
    }

    public void onWorldRender(){
        moduleList.forEach(Module::onWorldRender);
    }

    public void onKey(int eventKey) {
        if (eventKey == 0 || !Keyboard.getEventKeyState() || Safepoint.mc.currentScreen instanceof ClickGui)
            return;

        moduleList.forEach(module -> {
            if (module.getKeyBind() == eventKey) {
                if (module.isEnabled())
                    module.disableModule();
                else module.enableModule();
            }
        });
    }

    public Module find(Class clazz) {
        for (Module mod : this.getModuleList()) {
            if (mod.getClass() == clazz) {
                return mod;
            }
        }
        return null;
    }
}
