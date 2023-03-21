package safepoint.two.module.core;

import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.EnumSetting;

import java.util.Arrays;

@ModuleInfo(name = "Music", category = Module.Category.Core, description = "Edits the hud ye")
public class MusicPlayer extends Module {

    EnumSetting music = new EnumSetting("Music", "PuffinOnZooties", Arrays.asList("PuffinOnZooties", "BackInBlood", "HardToChoose"), this);
    BooleanSetting skip = new BooleanSetting("Skip", false, this);
    BooleanSetting shuffle = new BooleanSetting("Shuffle", false, this);

    @Override
    public void onEnable() {
        if(skip.getValue()){
            Safepoint.soundInitializer.skip();
            skip.setValue(false);
        }
        if(shuffle.getValue()){
            Safepoint.soundInitializer.shuffle();
        }
        Safepoint.soundInitializer.play();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Safepoint.soundInitializer.stop();
        super.onDisable();
    }
}
