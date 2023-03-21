package safepoint.two.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.core.event.events.PlayerAttackEvent;
import safepoint.two.core.settings.impl.IntegerSetting;
import safepoint.two.core.decentralized.interfac.INetworkManager;
import safepoint.two.utils.math.Timer;


@ModuleInfo(name = "Criticals", description = "", category = Module.Category.Combat)
public class Criticals extends Module {


    BooleanSetting packetMode = new BooleanSetting("Packet", true, this);
    FloatSetting jumpHeight = new FloatSetting("JumpHeight", 0.3f, 0.1f, 0.5f, this,v -> !packetMode.getValue());
    BooleanSetting onlyWeapon = new BooleanSetting("OnlyWeapon", true, this);
    BooleanSetting checkRaytrace = new BooleanSetting("Raytrace", true, this);
    BooleanSetting twoBee = new BooleanSetting("2b2t", false, this);
    BooleanSetting moveCancel = new BooleanSetting("MoveCancel", false, this);
    BooleanSetting noDesync = new BooleanSetting("NoDesync", false, this);
    BooleanSetting delayedAttack = new BooleanSetting("Delayed", false, this);
    IntegerSetting delay = new IntegerSetting("Delay", 3, 0, 20, this,v -> delayedAttack.getValue());
    BooleanSetting boats = new BooleanSetting("Boats", false, this);
    IntegerSetting hits = new IntegerSetting("Hits", 5, 1, 15, this,v -> boats.getValue());
    public static Criticals INSTANCE;

    Timer delayTimer = new Timer();

    private boolean flag2 = false;

    public boolean flag = false;

    private Entity target;

    public Criticals() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if(mc.player == null || mc.world == null){
            return;
        }
        if (!packetMode.getValue()) {
            if (target == null) return;

            if (flag && mc.player.fallDistance > 0.1 && canCrit() && (!checkRaytrace.getValue() || mc.objectMouseOver.entityHit == target)) {
                flag = false;
                mc.player.connection.sendPacket(new CPacketUseEntity(target));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                mc.player.resetCooldown();
            }

            if (flag && mc.player.fallDistance > 0.0)
                flag2 = true;

            if (flag2 && mc.player.onGround) {
                flag = false;
                flag2 = false;
                mc.player.connection.sendPacket(new CPacketUseEntity(target));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                mc.player.resetCooldown();
            }
        }

        if (packetMode.getValue() || (flag2 && (mc.player.onGround || mc.player.isInWeb || mc.player.isOnLadder() || mc.player.isRiding() ||
                mc.player.isPotionActive(MobEffects.BLINDNESS) || mc.player.isInWater() || mc.player.isInLava()))) {

            flag = false;

            flag2 = false;
        }
        if(anyMovementKeys() || isMoving() && moveCancel.getValue()){

            flag = false;

            flag2 = false;
        }
    }

    @SubscribeEvent
    public void onPlayerAttackPre(PlayerAttackEvent event) {
        if (!mc.gameSettings.keyBindJump.isKeyDown() && canCrit() && mc.player.onGround &&
                event.target instanceof EntityLivingBase && !flag) {

            if (event.target instanceof EntityEnderCrystal) {
                return;
            }

            if(delayedAttack.getValue() && delayTimer.passedMs(delay.getValue())){
                if (packetMode.getValue()) {
                    if (mc.player.getCooledAttackStrength(0.5f) > 0.9f) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    }
                    if(twoBee.getValue()){
                        sendCriticalPackets();
                    }
                }
                else if(noDesync.getValue()){
                    doJumpCrit();
                }else{
                    doJumpCrit();
                }
            }else{
                if (packetMode.getValue()) {
                    if (mc.player.getCooledAttackStrength(0.5f) > 0.9f) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    }
                    if(twoBee.getValue()){
                        sendCriticalPackets();
                    }
                }
                else if(noDesync.getValue()){
                    doJumpCrit();
                }else{
                    doJumpCrit();
                }
            }

        } else if (!mc.gameSettings.keyBindJump.isKeyDown() &&
                canCrit() &&
                mc.player.onGround &&
                event.target instanceof EntityBoat && !flag) {
            for (int i = 0; i < hits.getValue(); ++i) {
                ((INetworkManager) mc.getConnection().getNetworkManager()).sendPacketNoEvent(new CPacketUseEntity(event.target));
            }
        }
    }

    public void doJumpCrit() {
        mc.player.jump();

        mc.player.motionY = jumpHeight.getValue();
    }

    public boolean canCrit() {
        return (!mc.player.isInWeb && !mc.player.isOnLadder() && !mc.player.isRiding() &&
                !mc.player.isPotionActive(MobEffects.BLINDNESS) && !mc.player.isInWater() && !mc.player.isInLava()
                && (!onlyWeapon.getValue() || (isHoldingWeapon())));
    }

    private boolean isHoldingWeapon() {
        return mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD
                || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.STONE_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_AXE ||
                mc.player.getHeldItemMainhand().getItem() == Items.IRON_SWORD ||
                mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_AXE ||
                mc.player.getHeldItemMainhand().getItem() == Items.STONE_AXE ||
                mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_AXE;
    }

    void sendCriticalPackets() {
        if(mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown()){
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.3579E-6, mc.player.posZ, false));
        }
    }

    public static boolean isMoving() {
        return mc.player.moveForward != 0.0 || mc.player.moveStrafing != 0.0;
    }

    public static boolean anyMovementKeys() {
        return mc.player.movementInput.forwardKeyDown
                || mc.player.movementInput.backKeyDown
                || mc.player.movementInput.leftKeyDown
                || mc.player.movementInput.rightKeyDown
                || mc.player.movementInput.jump
                || mc.player.movementInput.sneak;
    }

}
