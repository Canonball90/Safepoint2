package safepoint.two.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.Safepoint;
import safepoint.two.core.decentralized.concurrent.repeat.RepeatUnit;
import safepoint.two.core.event.events.RenderRotationsEvent;
import safepoint.two.core.event.events.RotationEvent;
import safepoint.two.core.event.events.RotationUpdateEvent;
import safepoint.two.core.event.events.UpdateWalkingPlayerEvent;
import safepoint.two.core.initializers.RotationInitializer;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.*;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.world.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Aura", description = "", category = Module.Category.Combat)
public class Aura extends Module {

    public static Aura INSTANCE;

    public EntityLivingBase target = null;
    ParentSetting targetP =
            new ParentSetting("Target", false, this);
    EnumSetting targetMode =
            new EnumSetting("Mode","Distance", Arrays.asList("Distance", "Multi", "Health"), this).setParent(targetP);
    BooleanSetting players =
            new BooleanSetting("Player", true, this).setParent(targetP);
    BooleanSetting mobs =
            new BooleanSetting("Mobs", true, this).setParent(targetP);
    BooleanSetting ghast =
            new BooleanSetting("Ghast", true, this,v -> mobs.getValue()).setParent(targetP);
    BooleanSetting animals =
            new BooleanSetting("Animals", true, this).setParent(targetP);
    BooleanSetting projectile =
            new BooleanSetting("Projectiles", true, this).setParent(targetP);

    ParentSetting advancedTargeting =
            new ParentSetting("AdvancedTargeting", false, this);
    BooleanSetting advancedTarget =
            new BooleanSetting("AdvancedTarget", false, this).setParent(advancedTargeting);
    BooleanSetting Aplayers =
            new BooleanSetting("Players", false, this,v -> advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting aNoNaked =
            new BooleanSetting("AntiNaked", false, this,v -> Aplayers.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting aFriendProtect =
            new BooleanSetting("FriendProtect", false, this,v -> Aplayers.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);

    BooleanSetting Amobs =
            new BooleanSetting("Mobs", false, this,v -> advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting zombie =
            new BooleanSetting("Zombie", false, this,v -> Amobs.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting spider =
            new BooleanSetting("Spider", false, this,v -> Amobs.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting creeper =
            new BooleanSetting("Creeper", false, this,v -> Amobs.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting blaze =
            new BooleanSetting("Blaze", false, this,v -> Amobs.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting witch =
            new BooleanSetting("Witch", false, this,v -> Amobs.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting enderMan =
            new BooleanSetting("EnderMan", false, this,v -> Amobs.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);

    BooleanSetting Apassives =
            new BooleanSetting("Passives", false, this,v -> advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting sheep =
            new BooleanSetting("Sheep", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting cow =
            new BooleanSetting("Cow", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting pig =
            new BooleanSetting("Pig", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting rabbit =
            new BooleanSetting("Rabbit", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting donkey =
            new BooleanSetting("Donkey", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting horse =
            new BooleanSetting("Horse", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting chicken =
            new BooleanSetting("Chicken", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting villager =
            new BooleanSetting("Villager", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting squid =
            new BooleanSetting("Squid", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting bat =
            new BooleanSetting("Bat", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);
    BooleanSetting iromGolem =
            new BooleanSetting("IronGolem", false, this,v -> Apassives.getValue() && advancedTarget.getValue()).setParent(advancedTargeting);

    ParentSetting antiCheat =
            new ParentSetting("AntiCheat", false, this);
    BooleanSetting rotate =
            new BooleanSetting("Rotate", true, this).setParent(antiCheat);
    BooleanSetting packet =
            new BooleanSetting("Packet", false, this).setParent(antiCheat);
    BooleanSetting threaded =
            new BooleanSetting("Threaded", false, this).setParent(antiCheat);
    BooleanSetting click =
            new BooleanSetting("MouseClick", false, this).setParent(antiCheat);
    FloatSetting range =
            new FloatSetting("Range", 5f, 2f, 6f,this).setParent(antiCheat);
    BooleanSetting fovCheck =
            new BooleanSetting("FovCheck", false, this).setParent(antiCheat);
    FloatSetting angle =
            new FloatSetting("Angle", 180.0f, 0.0f, 180.0f, this, v -> fovCheck.getValue()).setParent(antiCheat);
    BooleanSetting raytrace =
            new BooleanSetting("RayTrace", false, this).setParent(antiCheat);
    EnumSetting raytracePart =
            new EnumSetting("RayTraceMode", "Body", Arrays.asList("Body", "Feet"), this).setParent(antiCheat);
    FloatSetting raytraceAmount =
            new FloatSetting("RaytraceAmount", 3.0f, 0.0f, 7.0f, this, v -> raytrace.getValue()).setParent(antiCheat);
    BooleanSetting stopSprint =
            new BooleanSetting("NoSprint", false, this).setParent(antiCheat);
    BooleanSetting tpsSync =
            new BooleanSetting("TpsSync", false, this).setParent(antiCheat);
    BooleanSetting disableOnCa =
            new BooleanSetting("ToggleOnCa", false, this).setParent(antiCheat);

    ParentSetting other =
            new ParentSetting("Other", false, this);
    BooleanSetting swordOnly =
            new BooleanSetting("OnlySword", true, this).setParent(other);
    BooleanSetting switchA =
            new BooleanSetting("Switch", false, this).setParent(other);
    BooleanSetting silent =
            new BooleanSetting("Silent", false, this,v -> switchA.getValue()).setParent(other);
    EnumSetting pWeapon =
            new EnumSetting("isPrefered", "Sword", Arrays.asList("Sword", "Axe", "Pickaxe", "Shovel", "None"), this).setParent(other);
    BooleanSetting armorBreak =
            new BooleanSetting("ArmorBreak", false, this).setParent(other);
    BooleanSetting hitbox =
            new BooleanSetting("HitBoxExpand", false, this).setParent(other);
    DoubleSetting hitboxSize =
            new DoubleSetting("HitBoxSize", 1, 0.1, 4, this, v -> hitbox.getValue()).setParent(other);
    EnumSetting hand =
            new EnumSetting("Hand", "Mainhand", Arrays.asList("Offhand", "Mainhand", "Both"), this).setParent(other);
    BooleanSetting resolve =
            new BooleanSetting("Resolver", false, this).setParent(other);
    BooleanSetting repeatUnit =
            new BooleanSetting("RepeatUnit", false, this).setParent(other);
    BooleanSetting eGravity =
            new BooleanSetting("Gravity", false, this).setParent(other);

    ParentSetting delays =
            new ParentSetting("Delays", false, this);
    IntegerSetting delay =
            new IntegerSetting("Delay", 4, 0, 70, this,v -> threaded.getValue() || packet.getValue()).setParent(delays);
    BooleanSetting randomD =
            new BooleanSetting("RandomDelay", false, this).setParent(delays);
    IntegerSetting randomDelay =
            new IntegerSetting("Random Delay", 4, 0, 40, this,v -> randomD.getValue()).setParent(delays);
    IntegerSetting iterations =
            new IntegerSetting("Iterations", 1, 1, 10, this).setParent(delays);
    BooleanSetting customDelays =
            new BooleanSetting("CustomDelays", false, this).setParent(delays);
    IntegerSetting Sword =
            new IntegerSetting("Sword", 600, 250, 1000, this,v -> customDelays.getValue()).setParent(delays);
    IntegerSetting Axe =
            new IntegerSetting("Axe", 1000, 250, 1500, this,v -> customDelays.getValue()).setParent(delays);
    IntegerSetting Shovel =
            new IntegerSetting("Shovel", 600, 250, 1000, this,v -> customDelays.getValue()).setParent(delays);
    IntegerSetting PickAxe =
            new IntegerSetting("PickAxe", 850, 250, 1000, this,v -> customDelays.getValue()).setParent(delays);
    IntegerSetting Hand =
            new IntegerSetting("Hand", 250, 250, 1000, this,v -> customDelays.getValue()).setParent(delays);

    ParentSetting targetStrafe =
            new ParentSetting("TargetStrafe", false, this);
    BooleanSetting tStrafe =
            new BooleanSetting("TargetStrafe", false, this).setParent(targetStrafe);
    BooleanSetting onKey =
            new BooleanSetting("OnKey", false, this).setParent(targetStrafe);
    KeySetting key =
            new KeySetting("Key", 0, this,v -> onKey.getValue()).setParent(targetStrafe);
    IntegerSetting speed =
            new IntegerSetting("Speed", 1, 0, 5, this,v -> tStrafe.getValue()).setParent(targetStrafe);

    ParentSetting rendering =
            new ParentSetting("Renders", false, this);
    BooleanSetting targetHud =
            new BooleanSetting("TargetHud", true, this).setParent(rendering);
    IntegerSetting Width =
            new IntegerSetting("Width", 200, 0, 500, this,v -> targetHud.getValue()).setParent(rendering);
    IntegerSetting Height =
            new IntegerSetting("Height", 200, 0, 500, this,v -> targetHud.getValue()).setParent(rendering);
    IntegerSetting healthbarSpeed =
            new IntegerSetting("AnimSpeed", 5, 1, 10, this,v -> targetHud.getValue()).setParent(rendering);
    BooleanSetting render =
            new BooleanSetting("Render", true, this).setParent(rendering);
    BooleanSetting cool =
            new BooleanSetting("Cool", true, this,v -> render.getValue()).setParent(rendering);
    ColorSetting color =
            new ColorSetting("Color", new Color(255,0,0,130), this,v -> render.getValue()).setParent(rendering);
    private final List<RepeatUnit> repeatUnits = new ArrayList<>();
    int waiting = 0;
    long killLast = new Date().getTime();
    double spoofedY = -1337;
    Timer timer = new Timer();
    float yaw;
    float pitch;
    public boolean sideDirection = true;
    public static int direction = -1;
    public double increment = 0.05;

    public Aura() {
        INSTANCE = this;
        repeatUnits.add(updateAura);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdateWalk(UpdateWalkingPlayerEvent event) {
        if (mc.player == null || mc.world == null) {return;}
        if(disableOnCa.getValue()){
            if(AutoCrystal.Instance.isEnabled()){
                disableModule();
            }
        }

        doKillAura();

        if(tStrafe.getValue()){
            try {
                if(onKey.getValue() && key.isTyping) {
                    if (mc.player == null) return;
                    if (mc.player.collidedHorizontally && timer.passedMs(80)) {
                        timer.reset();
                        invertStrafe();
                    }
                    mc.player.movementInput.moveForward = 0.0f;
                    double d = this.getMovementSpeed() + speed.getValue();
                    doStrafeAtSpeed(d);
                }else{
                    if (mc.player == null) return;
                    if (mc.player.collidedHorizontally && timer.passedMs(80)) {
                        timer.reset();
                        invertStrafe();
                    }
                    mc.player.movementInput.moveForward = 0.0f;
                    double d = this.getMovementSpeed() + speed.getValue();
                    doStrafeAtSpeed(d);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    RepeatUnit updateAura = new RepeatUnit(() -> 50, () -> {
        if (mc.player == null || mc.world == null) {return;}
        if(repeatUnit.getValue()) {
            doKillAura();
        }
    });

    @SubscribeEvent
    public void onHitbox(RenderWorldLastEvent e){
    if (this.hitbox.getValue() && this.isEnabled()) {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != null && player != mc.player) {
                player.setEntityBoundingBox(new AxisAlignedBB(
                        player.posX - hitboxSize.getValue(),
                        player.getEntityBoundingBox().minY,
                        player.posZ - hitboxSize.getValue(),
                        player.posX + hitboxSize.getValue(),
                        player.getEntityBoundingBox().maxY,
                        player.posZ + hitboxSize.getValue()
                ));
            }
        }
    } else {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != null && player != mc.player) {
                player.setEntityBoundingBox(new AxisAlignedBB(
                        player.posX - 0.3F,
                        player.getEntityBoundingBox().minY,
                        player.posZ - 0.3F,
                        player.posX + 0.3F,
                        player.getEntityBoundingBox().maxY,
                        player.posZ + 0.3F
                ));
            }
        }
    }
    }

    void doKillAura(){
        if (target != null) {
            if (target.getDistance(mc.player) >= range.getValue() || target.isDead || !target.isEntityAlive() || (aNoNaked.getValue() && isNakedPlayer(target)))
                target = null;
        }

        if(switchA.getValue()){
            InventoryUtil.switchToHotbarSlot(InventoryUtil.findItemInHotbar(preferredWeapon()), silent.getValue());
        }

        for (Entity entity : mc.world.loadedEntityList) {

            if (entity == mc.player) continue;

            if (entity.isDead || !entity.isEntityAlive()) continue;

            if (fovCheck.getValue() && !isInFov(entity, angle.getValue())) {
                continue;
            }

            if (!mc.player.canEntityBeSeen(entity) && !canEntityFeetBeSeen(entity) && mc.player.getDistanceSq(entity) > MathUtil.square(this.raytraceAmount.getValue())) {
                continue;
            }

            int delay = (int) (this.delay.getValue() *
                    10 + (randomD.getValue() ? this.randomDelay.getValue() * 10 * Math.random() : 0) *
                    (tpsSync.getValue() ? Safepoint.serverInitializer.getTpsFactor() : 1.0f));

            if (entity.getDistance(mc.player) <= range.getValue()) {

                if (timer.passedMs(threaded.getValue() || packet.getValue() ? delay : getHitCoolDown(mc.player))) {

                    if (entity instanceof EntityPlayer && players.getValue()) {

                        target = (EntityLivingBase) entity;

                    }
                    if (entity instanceof EntityAnimal && animals.getValue()) {
                        if(advancedTarget.getValue() &&
                                (sheep.getValue() && entity instanceof EntitySheep) ||
                                (cow.getValue() && entity instanceof EntityCow) ||
                                (pig.getValue() && entity instanceof EntityPig) ||
                                (rabbit.getValue() && entity instanceof EntityRabbit) ||
                                (donkey.getValue() && entity instanceof EntityDonkey) ||
                                (horse.getValue() && entity instanceof EntityHorse) ||
                                (chicken.getValue() && entity instanceof EntityChicken) ||
                                (villager.getValue() && entity instanceof EntityVillager) ||
                                (squid.getValue() && entity instanceof EntitySquid) ||
                                (bat.getValue() && entity instanceof EntityBat) ||
                                (iromGolem.getValue() && entity instanceof EntityIronGolem)) {
                            target = (EntityLivingBase) entity;
                        }else{
                            target = (EntityLivingBase) entity;
                        }
                    }
                    if ((entity instanceof EntityMob || entity instanceof EntitySlime) && mobs.getValue()) {
                        if(advancedTarget.getValue() &&
                                (zombie.getValue() && entity instanceof EntityZombie) ||
                                (spider.getValue() && entity instanceof EntitySpider) ||
                                (creeper.getValue() && entity instanceof EntityCreeper) ||
                                (blaze.getValue() && entity instanceof  EntityBlaze) ||
                                (witch.getValue() && entity instanceof EntityWitch) ||
                                (enderMan.getValue() && entity instanceof EntityEnderman)) {
                            target = (EntityLivingBase) entity;
                        }else{
                            target = (EntityLivingBase) entity;
                        }
                    }
                    if (isProjectile(entity) && projectile.getValue()) {
                        target = (EntityLivingBase) entity;
                    }
                    if ((entity instanceof EntityGhast) && ghast.getValue()) {
                        target = (EntityLivingBase) entity;
                    }

                    float[] arrf = RotationUtil.getRotations(target);

                    if (new Date().getTime() >= this.killLast + (threaded.getValue() || packet.getValue() ? delay : getHitCoolDown(mc.player))) {

                        this.killLast = new Date().getTime();

                        for (int i = 0; i < this.iterations.getValue(); ++i) {

                            if (resolve.getValue()) {
                                ResolverUtil.resolve((EntityOtherPlayerMP) target);
                            }

                            rayTrace(target);

                            if (eGravity.getValue() && !mc.player.onGround && mc.player.fallDistance > 0) {
                                mc.player.motionY -= 0.003;
                            }

                            if(rotate.getValue()){
                              RotationInitializer.lookAtTarget(target,false,2);
                            }

                            if (armorBreak.getValue()) {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                                attackeed(target, packet.getValue(), getHitCoolDown(mc.player));

                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                                attackeed(target, packet.getValue(), getHitCoolDown(mc.player));

                            } else {
                                if (timer.passedMs(delay)) {
                                    attackeed(target, threaded.getValue(), getHitCoolDown(mc.player));
                                }
                            }

                            if (target.isDead) {
                                ResolverUtil.reset();
                            }
                        }
                    }
                }
                if (stopSprint.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }
            }
        }
    }

    double healthBarTarget = 0, healthBar = 0;

    @SubscribeEvent
    public void onRenderRots(RenderRotationsEvent event){
        if(this.isEnabled() && rotate.getValue()){
            event.setPitch(pitch);
            event.setYaw(yaw);
        }
    }

    @SubscribeEvent
    public void rotationUpdate(RotationUpdateEvent event){
        if(this.isEnabled() && rotate.getValue()){
            event.setCanceled(true);
        }
    }


    @Override
    public void onDisable() {
        if(mc.player == null || mc.world == null){return;}
        RotationInitializer.resetRotation(false,2);
    }

    void attackeed(Entity ent, Boolean threaded, int time){
        if(targetMode.getValue().equalsIgnoreCase("Distance") || targetMode.getValue().equalsIgnoreCase("Health")){
            sortedAttack(ent,threaded,time);
        }
        if(targetMode.getValue().equalsIgnoreCase("Multi")){
            sortedMulti(ent,threaded,time);
        }
    }

    void sortedAttack(Entity ent, Boolean threaded, int time){
        List<Entity> closestPlayer = (List<Entity>) mc.world.loadedEntityList.stream().filter(player -> player.getDistance(mc.player) < range.getValue() && player != mc.player && player.isEntityAlive() && !Safepoint.friendInitializer.isFriend(player.getName())).collect(Collectors.toList());
        closestPlayer.sort(Comparator.comparingDouble(player -> (targetMode.getValue().equalsIgnoreCase("Distance") ? player.getDistanceSq(mc.player) : getHealth(ent))));
        if(!closestPlayer.isEmpty()) {
            Entity p = closestPlayer.get(0);
            attack(ent, threaded, time);
        }
    }

    void sortedMulti(Entity ent, Boolean threaded, int time) {
        for(Entity p : mc.world.loadedEntityList) {
            if(p instanceof Entity && p != mc.player) {
                if(mc.player.getDistance(p) <= range.getValue()) {
                    attack(ent, threaded, time);
                }
            }
        }
    }

    void attack(Entity ent, Boolean threaded, int time){
        if(threaded){
            startEntityAttackThread(ent, time);
        }else{
            attackTarget(ent);
        }
    }

    void attackTarget(Entity target){

        yaw = RotationUtil.getRotations(target)[0];
        pitch = RotationUtil.getRotations(target)[1];

        if(packet.getValue()) {
            if (swordOnly.getValue()) {
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                    if(rotate.getValue()){
                        RotationInitializer.lookAtTarget(target,false,2);
                    }
                    mc.playerController.connection.sendPacket(new CPacketUseEntity(target));
                    mc.player.swingArm(attackhand());
                }
            }else {
                if(rotate.getValue()){
                    RotationInitializer.lookAtTarget(target,false,2);
                }
                    mc.playerController.connection.sendPacket(new CPacketUseEntity(target));
                    mc.player.swingArm(attackhand());
                }
        } else {
            if (swordOnly.getValue()) {
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                    if(rotate.getValue()){
                        RotationInitializer.lookAtTarget(target,false,2);
                    }
                    mc.playerController.attackEntity(mc.player, target);
                    mc.player.swingArm(attackhand());
                }
            } else {
                if(rotate.getValue()){
                    RotationInitializer.lookAtTarget(target,false,2);
                }
                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(attackhand());
            }
        }

        if (rotate.getValue()) {
            RotationInitializer.lookAtTarget(target,false,2);
        }

    }

    @SubscribeEvent
    public void rotationEvent(RotationEvent event){
        event.setPitch(pitch);
        event.setYaw(yaw);
    }

    public void rayTrace(Entity e) {
        if(raytrace.getValue()) {
            if(raytracePart.getValue().equalsIgnoreCase("Body")) {
                raytraceEntity(e);
            } else if(raytracePart.getValue().equalsIgnoreCase("Feet")) {
                raytraceFeet(e);
            }
        }
    }

    private boolean isInFov(Entity entity, float angle) {
        double x = entity.posX - mc.player.posX;
        double z = entity.posZ - mc.player.posZ;
        double yaw = Math.atan2(x, z) * 57.29577951308232D;
        yaw = -yaw;
        angle = (float) (angle * 0.5D);
        double angleDifference = ((mc.player.rotationYaw - yaw) % 360.0D + 540.0D) % 360.0D - 180.0D;
        return ((angleDifference > 0.0D) && (angleDifference < angle)) || ((-angle < angleDifference) && (angleDifference < 0.0D));
    }

    private void startEntityAttackThread(Entity entity, int time) {
        new Thread(() -> {
            Timer timer = new Timer();
            timer.reset();
            try {
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            attackTarget(entity);
        }).start();
    }

    int getHitCoolDown(EntityPlayer player) {
        Item item = player.getHeldItemMainhand().getItem();

        if (item instanceof ItemSword) {
            return (customDelays.getValue() ? Sword.getValue() : 600);
        }
        if (item instanceof ItemPickaxe) {
            return (customDelays.getValue() ? PickAxe.getValue() : 850);
        }
        if (item == Items.IRON_AXE) {
            return (customDelays.getValue() ? Axe.getValue() : 1100);
        }
        if (item == Items.STONE_HOE) {
            return 500;
        }
        if (item == Items.IRON_HOE) {
            return 350;
        }
        if (item == Items.WOODEN_AXE || item == Items.STONE_AXE) {
            return (customDelays.getValue() ? Axe.getValue() : 1250);
        }
        if (item instanceof ItemSpade || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.WOODEN_HOE || item == Items.GOLDEN_HOE) {
            return (customDelays.getValue() ? Axe.getValue() : 1000);
        }
        return (customDelays.getValue() ? Hand.getValue() : 250);
    }

    Item preferredWeapon() {
        switch (pWeapon.getValue()) {
            case "Sword": {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_SWORD)) return Items.DIAMOND_SWORD;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_SWORD)) return Items.IRON_SWORD;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_SWORD)) return Items.STONE_SWORD;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_SWORD)) return Items.WOODEN_SWORD;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_SWORD)) return Items.GOLDEN_SWORD;
                        }
                    }
                }
            }

            case "Axe": {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_AXE)) return Items.DIAMOND_AXE;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_AXE)) return Items.IRON_AXE;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_AXE)) return Items.STONE_AXE;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_AXE)) return Items.WOODEN_AXE;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_AXE)) return Items.GOLDEN_AXE;
                        }
                    }
                }
            }

            case "PickAxe": {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_PICKAXE)) return Items.DIAMOND_PICKAXE;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_PICKAXE)) return Items.IRON_PICKAXE;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_PICKAXE)) return Items.STONE_PICKAXE;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_PICKAXE)) return Items.WOODEN_AXE;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_PICKAXE)) return Items.GOLDEN_PICKAXE;
                        }
                    }
                }
            }

            case "Shovel": {
                if (ItemUtils.isItemInHotbar(Items.DIAMOND_SHOVEL)) return Items.DIAMOND_SHOVEL;
                else {
                    if (ItemUtils.isItemInHotbar(Items.IRON_SHOVEL)) return Items.IRON_SHOVEL;
                    else {
                        if (ItemUtils.isItemInHotbar(Items.STONE_SHOVEL)) return Items.STONE_SHOVEL;
                        else {
                            if (ItemUtils.isItemInHotbar(Items.WOODEN_SHOVEL)) return Items.WOODEN_AXE;
                            else if (ItemUtils.isItemInHotbar(Items.GOLDEN_SHOVEL)) return Items.GOLDEN_SHOVEL;
                        }
                    }
                }
            }
        }
        
        return Items.AIR;
    }

    boolean checkPreferredWeapons() {
        if (mc.player != null) {
            switch (pWeapon.getValue()) {
                case "Sword":
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.STONE_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_SWORD || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_SWORD);

                case "Axe":
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_AXE || mc.player.getHeldItemMainhand().getItem() == Items.IRON_AXE || mc.player.getHeldItemMainhand().getItem() == Items.STONE_AXE || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_AXE || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_AXE);

                case "PickAxe":
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.IRON_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.STONE_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_PICKAXE || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_PICKAXE);

                case "Shovel":
                    return (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.STONE_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.WOODEN_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_SHOVEL);
            }
        }
        return false;
    }

    boolean canEntityFeetBeSeen(final Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posX + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }

    boolean isLiving(Entity entity) {
        return entity instanceof EntityLivingBase;
    }

    float getHealth(Entity entity) {
        if (isLiving(entity)) {
            EntityLivingBase livingBase = (EntityLivingBase) entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        }
        return 0.0f;
    }

    EnumHand attackhand(){
        switch(hand.getValue()){
            case "Mainhand":
                return EnumHand.MAIN_HAND;

            case "Offhand":
                return EnumHand.OFF_HAND;

            case "Both":
                return null;
        }
        return null;
    }

    boolean isProjectile(Entity entity) {
        return entity instanceof EntityShulkerBullet || entity instanceof EntityFireball;
    }

    boolean isNakedPlayer(EntityLivingBase entity) {
        if (!(entity instanceof EntityOtherPlayerMP)) {
            return false;
        }
        return entity.getTotalArmorValue() == 0;
    }

    //Render

    @Override
    public void onWorldRender() {
        if(target == null) return;
        if(target != null && isEnabled()) {
            if(render.getValue()) {
                if (!cool.getValue()) {
                    AxisAlignedBB box = target.getRenderBoundingBox().offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
                    RenderUtil.drawBlock(box, color.getValue());
                } else {
                    double everyTime = 1500;
                    double drawTime = (System.currentTimeMillis() % everyTime);
                    boolean drawMode = drawTime > (everyTime / 2);
                    double drawPercent = drawTime / (everyTime / 2);

                    if (!drawMode) {
                        drawPercent = 1 - drawPercent;
                    } else {
                        drawPercent -= 1;
                    }

                    drawPercent = easeInOutQuad(drawPercent);

                    mc.entityRenderer.disableLightmap();
                    glPushMatrix();
                    glDisable(GL_TEXTURE_2D);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    glEnable(GL_LINE_SMOOTH);
                    glEnable(GL_BLEND);

                    glDisable(GL_DEPTH_TEST);
                    glDisable(GL_CULL_FACE);
                    glShadeModel(7425);
                    mc.entityRenderer.disableLightmap();

                    double radius = target.width;
                    double height = target.height + 0.1;

                    double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosX;
                    double y = (target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosY) + height * drawPercent;
                    double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosZ;
                    double eased = (height / 3) * ((drawPercent > 0.5) ? 1 - drawPercent : drawPercent) * ((drawMode) ? -1 : 1);

                    for (int segments = 0; segments < 360; segments += 5) {
                        Color color1 = color.getValue();

                        double x1 = x - Math.sin(segments * Math.PI / 180F) * radius;
                        double z1 = z + Math.cos(segments * Math.PI / 180F) * radius;
                        double x2 = x - Math.sin((segments - 5) * Math.PI / 180F) * radius;
                        double z2 = z + Math.cos((segments - 5) * Math.PI / 180F) * radius;

                        glBegin(GL_QUADS);

                        glColor4f(pulseColor(color1, 200, 1).getRed() / 255.0f, pulseColor(color1, 200, 1).getGreen() / 255.0f, pulseColor(color1, 200, 1).getBlue() / 255.0f, 0.0f);
                        glVertex3d(x1, y + eased, z1);
                        glVertex3d(x2, y + eased, z2);

                        glColor4f(pulseColor(color1, 200, 1).getRed() / 255.0f, pulseColor(color1, 200, 1).getGreen() / 255.0f, pulseColor(color1, 200, 1).getBlue() / 255.0f, 200.0f);

                        glVertex3d(x2, y, z2);
                        glVertex3d(x1, y, z1);
                        glEnd();

                        glBegin(GL_LINE_LOOP);
                        glVertex3d(x2, y, z2);
                        glVertex3d(x1, y, z1);
                        glEnd();
                    }

                    glEnable(GL_CULL_FACE);
                    glShadeModel(7424);
                    glColor4f(1f, 1f, 1f, 1f);
                    glEnable(GL_DEPTH_TEST);
                    glDisable(GL_LINE_SMOOTH);
                    glDisable(GL_BLEND);
                    glEnable(GL_TEXTURE_2D);
                    glPopMatrix();
                }
            }
        }

        super.onWorldRender();
    }

    @SubscribeEvent
    public void onRender2d(RenderGameOverlayEvent.Text e){
        if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            if (target != null && targetHud.getValue()) {

                DecimalFormat dec = new DecimalFormat("#");

                healthBarTarget = Width.getValue() / 2 - 41 + (((140) / (target.getMaxHealth())) * (target.getHealth()));
                double HealthBarSpeed = healthbarSpeed.getValue();

                if (healthBar > healthBarTarget) {
                    healthBar = ((healthBar) - ((healthBar - healthBarTarget) / HealthBarSpeed));
                } else if (healthBar < healthBarTarget) {
                    healthBar = ((healthBar) + ((healthBarTarget - healthBar) / HealthBarSpeed));
                }
                int color = (target.getHealth() / target.getMaxHealth() > 0.66f) ? 0xff00ff00 : (target.getHealth() / target.getMaxHealth() > 0.33f) ? 0xffff9900 : 0xffff0000;

                color = 0xff00ff00;
                float[] hsb = Color.RGBtoHSB(((int) 255), ((int) 255), ((int) 255), null);
                float hue = hsb[0];
                float saturation = hsb[1];
                color = Color.HSBtoRGB(hue, saturation, 1);

                float hue1 = System.currentTimeMillis() % (int) ((100.5f - 50) * 1000) / (float) ((100.5f - 50) * 1000);
                color = Color.HSBtoRGB(hue1, 0.65f, 1);

                Gui.drawRect(Width.getValue() / 2 - 110, Height.getValue() / 2 + 100, Width.getValue() / 2 + 90, Height.getValue() / 2 + 170, 0xff36393f);
                Gui.drawRect(Width.getValue() / 2 - 41, Height.getValue() / 2 + 100 + 54, Width.getValue() / 2 + 80, Height.getValue() / 2 + 96 + 45, 0xff202225);
                Gui.drawRect(Width.getValue() / 2 - 41, Height.getValue() / 2 + 100 + 54, (int) healthBar, Height.getValue() / 2 + 96 + 45, color);
                GlStateManager.color(1, 1, 1);
                GuiInventory.drawEntityOnScreen(Width.getValue() / 2 - 75, Height.getValue() / 2 + 165, 25, 1f, 1f, target);
                mc.fontRenderer.drawString(target.getName(), Width.getValue() / 2 - 40, Height.getValue() / 2 + 110, -1);
                mc.fontRenderer.drawString("HP: ", Width.getValue() / 2 - 40, Height.getValue() / 2 + 125, -1);
                mc.fontRenderer.drawString("" + dec.format(target.getHealth()), Width.getValue() / 2 - 40 + mc.fontRenderer.getStringWidth("HP: "), Height.getValue() / 2 + 125, color);
            }
        }
    }

    static Color pulseColor(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((System.currentTimeMillis() % ((long) 1230675006 ^ 0x495A9BEEL) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (float) count * Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    double easeInOutQuad(double x) {
        return (x < 0.5) ? 2 * x * x : 1 - Math.pow((-2 * x + 2), 2) / 2;
    }

    public double getMovementSpeed() {
        double d = 0.2873;
        if (Minecraft.getMinecraft().player.isPotionActive(Objects.requireNonNull(Potion.getPotionById((int)1)))) {
            int n = Objects.requireNonNull(Minecraft.getMinecraft().player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById((int)1)))).getAmplifier();
            d *= 1.0 + 0.2 * (double)(n + 1);
        }
        return d;
    }

    public Entity getTargetEz() {
        if (mc.player == null || mc.player.isDead) {
            return null;
        }
        List list = mc.world.loadedEntityList.stream().filter(entity -> entity != mc.player).filter(entity -> mc.player.getDistance(entity) <= 7.0f).filter(entity -> !entity.isDead).filter(this::lambda$getTargetEz$3).sorted(Comparator.comparing(entity -> Float.valueOf(mc.player.getDistance(entity)))).collect(Collectors.toList());
        if (list.size() > 0) {
            return (Entity)list.get(0);
        }
        return null;
    }

    private boolean lambda$getTargetEz$3(Entity entity) {
        return this.attackCheck(entity);
    }

    public boolean attackCheck(Entity entity) {
        if (entity instanceof EntityPlayer && !entity.isInvisible()) {
            return true;
        }
        return entity instanceof EntityMob && !entity.isInvisible();
    }

    private void invertStrafe() {
        direction = -direction;
    }

    public boolean attackCheckin(Entity entity) {
        return entity instanceof EntityPlayer && ((EntityPlayer)entity).getHealth() > 0.0f && Math.abs(mc.player.rotationYaw - RotationUtil.getRotations((EntityLivingBase)entity)[0]) % 180.0f < 190.0f && !entity.isInvisible() && !entity.getUniqueID().equals(mc.player.getUniqueID());
    }

    public final boolean doStrafeAtSpeed(double d) {

        boolean bl = true;
        Entity entity = this.getTargetEz();

        if (entity != null) {
            if (mc.player.onGround) {

                mc.player.jump();

            }

            float[] arrf = RotationUtil.getRotations((EntityLivingBase)entity);

            if ((double) Minecraft.getMinecraft().player.getDistance(entity) <= 3) {

                mc.player.renderYawOffset = arrf[0];
                mc.player.rotationYawHead = arrf[0];
                setSpeed(d - (0.1 - 7 / 100.0), arrf[0], direction, 0.0);

            } else {

                setSpeed(d - (0.1 - 7 / 100.0), arrf[0], direction, 1.0);
                mc.player.renderYawOffset = arrf[0];
                mc.player.rotationYawHead = arrf[0];

            }
        }
        return bl;
    }

    public static void setSpeed(double d, float f, double d2, double d3) {

        double d4 = d3;
        double d5 = d2;
        float f2 = f;

        if (d4 == 0.0 && d5 == 0.0) {
            mc.player.motionZ = 0.0;
            mc.player.motionX = 0.0;
        } else {
            if (d4 != 0.0) {
                if (d5 > 0.0) {
                    f2 += (float)(d4 > 0.0 ? -45 : 45);
                } else if (d5 < 0.0) {
                    f2 += (float)(d4 > 0.0 ? 45 : -45);
                }
                d5 = 0.0;
                if (d4 > 0.0) {
                    d4 = 1.0;
                } else if (d4 < 0.0) {
                    d4 = -1.0;
                }
            }
            double d6 = Math.cos(Math.toRadians(f2 + 90.0f));
            double d7 = Math.sin(Math.toRadians(f2 + 90.0f));

            mc.player.motionX = d4 * d * d6 + d5 * d * d7;
            mc.player.motionZ = d4 * d * d7 - d5 * d * d6;
        }
    }

    public boolean raytraceBlock(BlockPos blockPos, double offset) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX(), blockPos.getY() + offset, blockPos.getZ()), false, true, false) == null;
    }

    public boolean raytraceQuill(BlockPos blockPos, double offset) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX(), blockPos.getY() + offset + 1.5, blockPos.getZ()), false, true, false) == null;
    }

    public boolean raytraceEntity(Entity entity) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entity.posX, entity.posY, entity.posZ), false, true, false) == null;
    }

    public boolean raytraceFeet(Entity entity) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entity.posX, entity.posY, entity.posZ), false, true, false) == null;
    }

    enum ThreadType{
        PLAYER
    }

    final class Threads extends Thread {

        ThreadType type;

        Entity bestEnt;

        public Threads(ThreadType type) {
            this.type = type;
        }

        @Override
        public void run() {
            if (this.type == ThreadType.PLAYER) {

                mc.thread.run();

                if(mc.isCallingFromMinecraftThread()){

                    bestEnt = Aura.INSTANCE.target;

                }
            }
        }

    }
}