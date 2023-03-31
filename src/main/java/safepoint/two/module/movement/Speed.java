package safepoint.two.module.movement;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.core.event.events.EventMove;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.utils.world.PlayerUtil;

import java.util.Arrays;

@ModuleInfo(name = "Speed", description = "",category = Module.Category.Movement)
public class Speed extends Module {
    private int strafeStage;
    private double baseSpeed = 0.2873;
    private double moveSpeed;
    private double latestMoveSpeed;
    private boolean accelerate;

    EnumSetting mode = new EnumSetting("Mode", "yPort", Arrays.asList("5B5T", "yPort", "Strafe"), this);
    BooleanSetting strict = new BooleanSetting("Strict", true, this);
    BooleanSetting timer = new BooleanSetting("Timer", true, this);

    @Override
    public void onEnable() {
        strafeStage = 4;
    }

    @Override
    public void onDisable() {
        reset();
    }

    public void reset() {
        strafeStage = 4;
        moveSpeed = 0;
        mc.timer.tickLength = 50f;
        latestMoveSpeed = 0;
        accelerate = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) {return;}
        if(mode.getValue().equalsIgnoreCase("yPort")) {
            if(PlayerUtil.isMoving()) {
                if (!(mc.gameSettings.keyBindJump.isKeyDown()  || mc.player.collidedHorizontally)) {

                    if (mc.player.onGround) {
                        mc.timer.tickLength = 50 / 1.15f;
                        mc.player.jump();
                        PlayerUtil.strafe((float) (getBaseMoveSpeed() + 0.1));
                    } else {
                        mc.player.motionY = -1;
                        mc.timer.tickLength = 50f;
                    }
                }
            }
        }else if(mode.getValue().equalsIgnoreCase("5B5T")) {
            if(mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown()){
                mc.player.motionY = 0;
                PlayerUtil.strafe(0.285f);
                mc.timer.tickLength = 50 / 1.15f;
            }
        }
    }


    @SubscribeEvent
    public void onMotion(EventMove event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mode.getValue().equalsIgnoreCase("Strafe")) {

            latestMoveSpeed = Math.sqrt(StrictMath.pow(mc.player.posX - mc.player.prevPosX, 2) + StrictMath.pow(mc.player.posZ - mc.player.prevPosZ, 2));
            double motionY = strict.getValue() ? 0.41999998688697815 : 0.3999999463558197;

            if (timer.getValue()) {
                mc.timer.tickLength = 50 / 1.088f;
            } else {
                mc.timer.tickLength = 50f;
            }

            if (PlayerUtil.isMoving()) {
                if (strafeStage == 1) {
                    moveSpeed = 1.35 * baseSpeed - 0.01;
                } else if (strafeStage == 2) {
                    if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                        double amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
                        motionY += (amplifier + 1) * 0.1;
                    }

                    mc.player.motionY = motionY;
                    event.setY(motionY);

                    double acceleration = 1.395;

                    if (accelerate) {
                        acceleration = 1.6835;
                    }

                    moveSpeed *= acceleration;
                } else if (strafeStage == 3) {

                    double scaledMoveSpeed = 0.66 * (latestMoveSpeed - baseSpeed);

                    moveSpeed = latestMoveSpeed - scaledMoveSpeed;

                    accelerate = !accelerate;
                } else {
                    if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, mc.player.motionY, 0)).size() > 0 || mc.player.collidedVertically) && strafeStage > 0) {

                        strafeStage = PlayerUtil.isMoving() ? 1 : 0;
                    }

                    moveSpeed = latestMoveSpeed - (latestMoveSpeed / 159);
                }

                moveSpeed = Math.max(moveSpeed, baseSpeed);
                float forward = mc.player.movementInput.moveForward;
                float strafe = mc.player.movementInput.moveStrafe;
                float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

                if (!PlayerUtil.isMoving()) {
                    event.setX(0);
                    event.setZ(0);
                } else if (forward != 0) {
                    if (strafe > 0) {
                        yaw += forward > 0 ? -45 : 45;
                    } else if (strafe < 0) {
                        yaw += forward > 0 ? 45 : -45;
                    }

                    strafe = 0;

                    if (forward > 0) {
                        forward = 1;
                    } else if (forward < 0) {
                        forward = -1;
                    }
                }

                double cos = Math.cos(Math.toRadians(yaw));
                double sin = -Math.sin(Math.toRadians(yaw));

                event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));

                strafeStage++;

            }
        }
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (mc.player != null && mc.player.isPotionActive(Potion.getPotionById(1))) {
            final int amplifier = mc.player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }
}
