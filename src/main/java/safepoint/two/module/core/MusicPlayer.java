package safepoint.two.module.core;

import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;

@ModuleInfo(name = "Music", category = Module.Category.Core, description = "Edits the hud ye")
public class MusicPlayer extends Module {

    @Override
    public void onEnable() {
        Safepoint.soundInitializer.play();
        super.onTick();
    }

    @Override
    public void onDisable() {
        Safepoint.soundInitializer.stop();
        super.onDisable();
    }
}
