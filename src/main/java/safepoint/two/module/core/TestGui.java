package safepoint.two.module.core;

import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.guis.testgui.ClickUi;

@ModuleInfo(name = "TestGui", category = Module.Category.Core, description = "Edits the hud ye")
public class TestGui extends Module {

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new ClickUi());
        super.onEnable();
    }
}
