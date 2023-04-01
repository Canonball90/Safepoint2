package safepoint.two.module.movement;

import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.utils.core.MappingUtil;
import safepoint.two.utils.world.PlayerUtil;

import java.lang.reflect.Field;
import java.util.Arrays;

@ModuleInfo(name = "AntiWeb", category = Module.Category.Movement, description = "")
public class AntiWeb extends Module {

    public EnumSetting mode = new EnumSetting("Mode","Solid", Arrays.asList("Solid", "Normal", "Timer", "Step"), this);
    public BooleanSetting holeOnly = new BooleanSetting("HoleOnly", true, this,v -> mode.getValue().equalsIgnoreCase("Timer"));
    public FloatSetting timerSpeed = new FloatSetting("TimerSpeed", 4.0f,0.1f, 50.0f, this,v -> mode.getValue().equalsIgnoreCase("Timer"));
    public BooleanSetting onGround = new BooleanSetting("OnGround", false, this);
    public DoubleSetting motionY = new DoubleSetting("MotionY", 1.0, 0.0, 20.0, this);
    public DoubleSetting motionX = new DoubleSetting("MotionX", 1.0, 0.0, 20.0, this);
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
        if(onGround.getValue() && mode.getValue().equalsIgnoreCase("Step")){
            mc.player.onGround = true;
        }
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
        }else if (mc.player.isInWeb && mode.getValue().equalsIgnoreCase("Step")) {
            if (Keyboard.isKeyDown((int)mc.gameSettings.keyBindSneak.keyCode)) {
                mc.player.isInWeb = true;
                mc.player.motionY *= this.motionY.getValue().doubleValue();
            } else if (this.onGround.getValue().booleanValue()) {
                mc.player.onGround = false;
            }
            if (Keyboard.isKeyDown((int)mc.gameSettings.keyBindForward.keyCode) || Keyboard.isKeyDown((int)mc.gameSettings.keyBindBack.keyCode) || Keyboard.isKeyDown((int)mc.gameSettings.keyBindLeft.keyCode) || Keyboard.isKeyDown((int)mc.gameSettings.keyBindRight.keyCode)) {
                mc.player.isInWeb = false;
                mc.player.motionX *= this.motionX.getValue().doubleValue();
                mc.player.motionZ *= this.motionX.getValue().doubleValue();
            }
        }
    }
}
