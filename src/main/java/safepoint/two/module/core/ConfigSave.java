package safepoint.two.module.core;

import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.utils.core.ChatUtil;

@ModuleInfo(name = "ConfigSaver", description = "", category = Module.Category.Core)
public class ConfigSave extends Module {

    @Override
    public void onEnable() {
        Safepoint.configInitializer.save();
        Safepoint.configInitializer.saveHudState();
        Safepoint.configInitializer.saveModuleFile();
        Safepoint.configInitializer.saveFriendList();
        ChatUtil.send("Config Saved!");
        disableModule();
    }
}
