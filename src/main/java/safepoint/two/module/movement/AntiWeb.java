package safepoint.two.module.movement;

import net.minecraft.entity.Entity;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.utils.core.MappingUtil;
import safepoint.two.utils.world.PlayerUtil;

import java.lang.reflect.Field;
import java.util.Arrays;

@ModuleInfo(name = "AntiWeb", category = Module.Category.Movement, description = "")
public class AntiWeb extends Module {

    public EnumSetting mode = new EnumSetting("Mode","Solid", Arrays.asList("Solid", "Normal", "Timer"), this);
    public BooleanSetting holeOnly = new BooleanSetting("HoleOnly", true, this,v -> mode.getValue().equalsIgnoreCase("Timer"));
    public FloatSetting timerSpeed = new FloatSetting("TimerSpeed", 4.0f,0.1f, 50.0f, this,v -> mode.getValue().equalsIgnoreCase("Timer"));
    public float speed;
    public static AntiWeb INSTANCE;

    public AntiWeb(){
        this.speed = 1.0f;
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.speed = this.timerSpeed.getValue();
        super.onEnable();
    }

    @Override
    public void onTick() {
        if(mode.getValue().equalsIgnoreCase("Normal")) {
            try {
                Field isInWeb = Entity.class.getDeclaredField(MappingUtil.isInWeb);
                isInWeb.setAccessible(true);
                isInWeb.setBoolean(mc.player, false);
            } catch (Exception ex) {
                this.disableModule();
            }
        }else if (mode.getValue().equalsIgnoreCase("Timer")){
            if (this.holeOnly.getValue()) {
                if (mc.player.isInWeb && PlayerUtil.isPlayerInHole()) {
                    mc.timer.tickLength = 50.0f / ((this.timerSpeed.getValue() == 0.0f) ? 0.1f : this.timerSpeed.getValue());
                }
                else {
                    mc.timer.tickLength = 50.0f;
                }
                if (mc.player.onGround && PlayerUtil.isPlayerInHole()) {
                    mc.timer.tickLength = 50.0f;
                }
            }
            if (!this.holeOnly.getValue()) {
                if (mc.player.isInWeb) {
                    mc.timer.tickLength = 50.0f / ((this.timerSpeed.getValue() == 0.0f) ? 0.1f : this.timerSpeed.getValue());
                }
                else {
                    mc.timer.tickLength = 50.0f;
                }
                if (mc.player.onGround) {
                    mc.timer.tickLength = 50.0f;
                }
            }
        }
    }
}
