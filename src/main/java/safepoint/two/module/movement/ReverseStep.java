package safepoint.two.module.movement;

import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.IntegerSetting;

@ModuleInfo(name = "ReverseStep", category = Module.Category.Movement, description = "Reverse step module \nbasically pushes you down")
public class ReverseStep extends Module {

    IntegerSetting power = new IntegerSetting("Double", 10, 1, 100, this);

    @Override
    public void onTick() {
        if(mc.player == null || mc.world == null) return;
        if (mc.player.onGround && !mc.player.inWater && !mc.player.isInLava() && !mc.player.isOnLadder() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = -power.value;
        }
        super.onTick();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
