package safepoint.two.module.core;

import safepoint.two.guis.hud.HudWindow;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;

@ModuleInfo(name = "Hud Editor", category = Module.Category.Core, description = "Edits the hud ye")
public class HudEditor extends Module {

    @Override
    public void onEnable() {
        mc.displayGuiScreen(HudWindow.getInstance());
        disableModule();
    }
}
