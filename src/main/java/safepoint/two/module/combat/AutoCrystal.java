package safepoint.two.module.combat;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.*;
import safepoint.two.core.initializers.RotationInitializer;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.*;
import safepoint.two.mixin.mixins.AccessorCPacketPlayer;
import safepoint.two.mixin.mixins.AccessorCPacketUseEntity;
import safepoint.two.mixin.mixins.ICPacketUseEntity;
import safepoint.two.utils.Utils;
import safepoint.two.utils.crystal.CrystalUtils;
import safepoint.two.utils.math.Inhibitator;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.world.BlockUtil;
import safepoint.two.utils.world.PlayerUtil;
import safepoint.two.utils.world.RotationUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.minecraft.network.play.client.CPacketUseEntity.Action.ATTACK;

@ModuleInfo(name = "AutoCrystal", category = Module.Category.Combat, description = "Places and breaks crystals")
public class AutoCrystal extends Module {

    //place
    ParentSetting Pplace = new ParentSetting("Place", false, this);
    BooleanSetting place = new BooleanSetting("Place", true, this).setParent(Pplace);
    BooleanSetting predict = new BooleanSetting("Predict", true, this).setParent(Pplace);
    BooleanSetting newPlacement = new BooleanSetting("1.13+", false, this).setParent(Pplace);
    IntegerSetting faceplace = new IntegerSetting("FacePlace",  8, 0, 36, this).setParent(Pplace);

    //break
    ParentSetting Bbreak = new ParentSetting("Break", false, this);
    DoubleSetting hitDelay = new DoubleSetting("HitDelay", 0, -6, 600, this).setParent(Bbreak);
    BooleanSetting ak47 = new BooleanSetting("Ak47", true, this).setParent(Bbreak);
    BooleanSetting setDead = new BooleanSetting("SetDead", true, this).setParent(Bbreak);
    BooleanSetting instantExplode = new BooleanSetting("Instant", true, this).setParent(Bbreak);
    DoubleSetting packetAmount  = new DoubleSetting("Packets", 1, 1, 20, this,v -> instantExplode.getValue()).setParent(Bbreak);

    //render
    ParentSetting rendor = new ParentSetting("Render", false, this);
    ColorSetting boxColor = new ColorSetting("Color", new Color(255,0,0,100), this).setParent(rendor);
    ColorSetting outlineColor = new ColorSetting("Color", new Color(255,0,0,100), this).setParent(rendor);
    BooleanSetting slab = new BooleanSetting("Slab", false, this).setParent(rendor);
    FloatSetting height = new FloatSetting("Height", 0.8f, -1.5f, 3, this, v -> slab.getValue()).setParent(rendor);
    FloatSetting lineWidth = new FloatSetting("LineWidth", 0.8f, -1.5f, 3, this).setParent(rendor);
    BooleanSetting pulse = new BooleanSetting("Pulse", false, this).setParent(rendor);
    FloatSetting pulseMax = new FloatSetting("Pulse Max", 1f, 0.0f, 1.5f, this, v -> pulse.getValue()).setParent(rendor);
    FloatSetting pulseMin = new FloatSetting("Pulse Min", 0.5f, 0.0f, 1.5f, this, v -> pulse.getValue()).setParent(rendor);
    FloatSetting pulseSpeed = new FloatSetting("Pulse Speed", 4.0f, 0.0f, 5.0f, this, v -> pulse.getValue()).setParent(rendor);
    FloatSetting rollingWidth = new FloatSetting("Pulse W", 8.0f, 0.0f, 20.0f, this, v -> pulse.getValue()).setParent(rendor);
    BooleanSetting fade = new BooleanSetting("Fade", true, this).setParent(rendor);
    IntegerSetting fadeSpeed = new IntegerSetting("Fade Speed", 20, 0, 100,this,v -> fade.getValue()).setParent(rendor);
    IntegerSetting startAlpha = new IntegerSetting("Start Alpha", 255, 0, 255, this,v -> fade.getValue()).setParent(rendor);
    IntegerSetting endAlpha = new IntegerSetting("End Alpha", 255, 0, 255, this,v -> fade.getValue()).setParent(rendor);
    BooleanSetting box = new BooleanSetting("Box", false, this).setParent(rendor);
    BooleanSetting outline = new BooleanSetting("Outline", false, this).setParent(rendor);

    //other
    ParentSetting other = new ParentSetting("Other", false, this);
    EnumSetting logic = new EnumSetting("Logic", "BREAKPLACE",  Arrays.asList("BREAKPLACE", "PLACEBREAK"), this).setParent(other);
    BooleanSetting spoofRotations = new BooleanSetting("SpoofRotations", true, this).setParent(other);
    BooleanSetting autoSwitch = new BooleanSetting("AutoSwitch", true, this).setParent(other);
    IntegerSetting range = new IntegerSetting("Range",  5, 0, 6, this).setParent(other);
    BooleanSetting wall = new BooleanSetting("Walls", true, this).setParent(other);
    IntegerSetting walls = new IntegerSetting("WallRange", 3, 0, 4, this,v -> wall.getValue()).setParent(other);
    IntegerSetting enemyRange = new IntegerSetting("EnemyRange",  12, 5, 15, this).setParent(other);
    IntegerSetting placeRange = new IntegerSetting("PlaceRange", 5, 0, 6, this).setParent(other);
    IntegerSetting maxSelfDmg = new IntegerSetting("MaxSeldDMG",  8, 0, 36, this).setParent(other);
    IntegerSetting minDmg = new IntegerSetting("MinDMG", 8, 0, 20, this).setParent(other);
    BooleanSetting limitAttack = new BooleanSetting("Limit Attack", false, this).setParent(other);
    BooleanSetting inhibit = new BooleanSetting("Inhibit", false, this).setParent(other);
    DoubleSetting startVal = new DoubleSetting("StartVal",200, 1, 1000, this,v -> inhibit.getValue()).setParent(other);
    DoubleSetting endVal = new DoubleSetting("EndVal",400, 1, 1000, this,v -> inhibit.getValue()).setParent(other);
    BooleanSetting disableWhenKA = new BooleanSetting("ToggleOnKa", false, this).setParent(other);

    //Rotation
    ParentSetting rotations = new ParentSetting("Rotations", false, this);
    BooleanSetting rotate = new BooleanSetting("Rotate", true, this).setParent(rotations);
    BooleanSetting breakRotate = new BooleanSetting("BreakRotate", true, this,v -> rotate.getValue()).setParent(rotations);
    BooleanSetting placeRotate = new BooleanSetting("PlaceRotate", true, this,v -> rotate.getValue()).setParent(rotations);

    //Predict
    ParentSetting prediction = new ParentSetting("Predictions", false, this);
    BooleanSetting soundPredict = new BooleanSetting("Sound Predict", false, this).setParent(prediction);
    BooleanSetting breakPredict = new BooleanSetting("Break Predict", false, this).setParent(prediction);
    BooleanSetting breakPredictCalc = new BooleanSetting("Break Predict Calc", false, this, v -> breakPredict.getValue()).setParent(prediction);
    BooleanSetting globalEntitySpawnPredict = new BooleanSetting("Global Entity Spawn Predict", false, this).setParent(prediction);
    BooleanSetting spawnObject = new BooleanSetting("Spawn Object", false, this).setParent(prediction);
    IntegerSetting predictDelay = new IntegerSetting("Predict Delay", 100, 0, 500, this).setParent(prediction);

    //Threading
    ParentSetting thred = new ParentSetting("Threading", false, this);
    BooleanSetting thread = new BooleanSetting("Thread", false, this).setParent(thred);
    EnumSetting threadMode = new EnumSetting("ThreadMode", "Run", Arrays.asList("Run", "Sleep"), this,v -> thread.getValue()).setParent(thred);
    BooleanSetting cpuOptimize = new BooleanSetting("CPU Optimize", false, this).setParent(thred);
    EnumSetting mode = new EnumSetting("Mode", "RenderTick", Arrays.asList("RenderTick", "PWUpdate", "Thread"), this).setParent(thred);
    private final List<Integer> deadCrystals = new ArrayList<>();
    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<>();
    transient AtomicInteger lastEntityId = new AtomicInteger(-1);
    HashMap<Integer, Entity> attemptedEntityId = new HashMap();
    HashMap<BlockPos, Integer> possesToFade = new HashMap();
    Inhibitator inhibitator = new Inhibitator();
    transient private static boolean rotating = false;
    transient public static float yaw;
    transient public static float pitch;
    transient public static float renderPitch;
    transient public static boolean shouldSpoofPacket;
    public static EntityPlayer target2;
    BlockPos render;
    BlockPos pos = null;
    String damageString;
    Timer breakTimer = new Timer();
    Timer predictTimer = new Timer();
    boolean mainhand = false;
    boolean offhand = false;
    public static AutoCrystal Instance;

    public AutoCrystal() {
        Instance = this;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.RenderTickEvent event) {
        if (mc.player == null || mc.world == null)
            return;
        if(disableWhenKA.getValue()){
            if(Aura.INSTANCE.isEnabled()){
                disableModule();
            }
        }
        if (inhibit.getValue()) {
            inhibitator.doInhibitation(hitDelay,0,endVal.getValue(),startVal.getValue(),5);
        }
        if(mode.getValue().equalsIgnoreCase("RenderTick")) {
            if(thread.getValue()) {
                if (threadMode.getValue().equalsIgnoreCase("Sleep")) {
                    startCrystalBreakThread(hitDelay.getValue().intValue());
                }
            }
            else{
                dologic();
            }
        }
    }

    @SubscribeEvent
    public void onRoot(RootEvent event) {
        if (mc.player == null || mc.world == null)
            return;
        if (inhibit.getValue()) {
            inhibitator.doInhibitation(hitDelay,0,endVal.getValue(),startVal.getValue(),5);
        }
        if(mode.getValue().equalsIgnoreCase("Thread")) {
            if(thread.getValue()) {
                if (threadMode.getValue().equalsIgnoreCase("Sleep")) {
                    startCrystalBreakThread(hitDelay.getValue().intValue());
                }
            }
            else{
                dologic();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerWalk(UpdateWalkingPlayerEvent event){
        if (mc.player == null || mc.world == null)
            return;
        if (inhibit.getValue()) {
            inhibitator.doInhibitation(hitDelay,0,endVal.getValue(),startVal.getValue(),5);
        }
        if(mode.getValue().equalsIgnoreCase("PWUpdate")) {
            if(thread.getValue()) {
                if (threadMode.getValue().equalsIgnoreCase("Sleep")) {
                    startCrystalBreakThread(hitDelay.getValue().intValue());
                }
            }
            else{
                dologic();
            }
        }
    }

    private void startCrystalBreakThread(int time) {
        new Thread(() -> {
            Timer timer = new Timer();
            timer.reset();
            try {
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            dologic();
        }).start();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event){
        CPacketUseEntity packet = new CPacketUseEntity();
        if (setDead.getValue() && event.getStage() == 0 && event.getPacket() instanceof CPacketUseEntity && (packet = event.getPacket()).getAction() == ATTACK && packet.getEntityFromWorld(AutoCrystal.mc.world) instanceof EntityEnderCrystal) {
            Entity entity = (EntityEnderCrystal) packet.getEntityFromWorld(mc.world);
            if (entity.isAddedToWorld()) {
                entity.setDead();
                mc.world.removeEntityFromWorld(entity.entityId);
            }
        }
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet1 = (CPacketPlayer) event.getPacket();
            if (shouldSpoofPacket) {
                ((AccessorCPacketPlayer) packet1).setYaw(yaw);
                ((AccessorCPacketPlayer) packet1).setPitch(pitch);
                shouldSpoofPacket = false;
            }
        }
    }

    private void resetRotation() {
        if (shouldSpoofPacket) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            shouldSpoofPacket = false;
            rotating = false;
        }
    }


    public void lookAtCrystal(EntityEnderCrystal ent) {
        float[] v = RotationUtil.getRotations(ent);
        float[] v2 = RotationUtil.getRotations(ent);
        setYawAndPitch(v[0], v[1], v2[1]);
    }

    public void lookAtPos(BlockPos block, EnumFacing face) {
        float[] v = RotationUtil.getRotationsBlock(block, face, false);
        float[] v2 = RotationUtil.getRotationsBlock(block.add(0, +0.5, 0), face, false);
        setYawAndPitch(v[0], v[1], v2[1]);
    }


    public void setYawAndPitch(float yaw1, float pitch1, float renderPitch1) {
        yaw = yaw1;
        pitch = pitch1;
        renderPitch = renderPitch1;
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
        shouldSpoofPacket = true;
        rotating = true;
    }


    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.Receive event){
        if (breakPredict.getValue() && event.getPacket() instanceof SPacketSpawnObject && predictTimer.passedMs(predictDelay.getValue())) {
            final SPacketSpawnObject packet = event.getPacket();
            if (packet.getType() == 51 && pos != null && PlayerUtil.getTarget(range.getValue()) != null) {
                final CPacketUseEntity predict = new CPacketUseEntity();
                predict.entityId = packet.getEntityID();
                predict.action = ATTACK;

//                if (breakPredictCalc.getValue() && !isPosGood(new BlockPos(packet.getX(), packet.getY(), packet.getZ())))
//                    return;

                mc.getConnection().sendPacket(predict);

                MinecraftForge.EVENT_BUS.post(new CrystalAttackEvent(predict.entityId, predict.getEntityFromWorld(mc.world)));
                predictTimer.reset();
            }
        }
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = event.getPacket();
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                java.util.List<Entity> loadedEntityList = mc.world.loadedEntityList;
                if (!loadedEntityList.isEmpty())
                    for (Entity entity : loadedEntityList) {
                        if (entity == null)
                            continue;
                        if (!(entity instanceof EntityEnderCrystal))
                            continue;

                        if (limitAttack.getValue() && attemptedEntityId.containsValue(entity.getEntityId()))
                            attemptedEntityId.remove(entity, entity.getEntityId());

                        if (entity.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) <= MathUtil.square(range.getValue()))
                            entity.setDead();

                    }
            }
        }
        SPacketSpawnObject spawnedCrystal = new SPacketSpawnObject();
        if (event.getPacket() instanceof SPacketSpawnObject && (spawnedCrystal = event.getPacket()).getType() == 51 && this.instantExplode.getValue()) {
            CPacketUseEntity attackPacket = new CPacketUseEntity();
            ((ICPacketUseEntity) attackPacket).setEntityId(spawnedCrystal.getEntityID());
            ((ICPacketUseEntity) attackPacket).setAction(ATTACK);

            for (int i = 1; i <= packetAmount.getValue(); i++) {
                mc.player.connection.sendPacket(attackPacket);
            }
            lastEntityId.getAndUpdate(it -> Math.max(it, ((SPacketSpawnObject) (event.getPacket())).getEntityID()));
            if (inhibit.getValue()) packetBreak(mc.player, (SPacketSpawnObject) event.getPacket());
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (this.render != null && target2 != null) {
            if (fade.getValue()) {
                for (Map.Entry<BlockPos, Integer> entry : possesToFade.entrySet()) {
                    possesToFade.put(entry.getKey(), entry.getValue() - (fadeSpeed.getValue() / 10));
                    if (entry.getValue() <= endAlpha.getValue()) {
                        possesToFade.remove(entry.getKey());
                        return;
                    }
                    RenderUtil.drawBoxESP(entry.getKey(), new Color(boxColor.getColor().getRed() / 255f, boxColor.getColor().getGreen() / 255f, boxColor.getColor().getBlue() / 255f, entry.getValue() / 255f), true, new Color(outlineColor.getColor().getRed() / 255f, outlineColor.getColor().getGreen() / 255f, outlineColor.getColor().getBlue() / 255f, entry.getValue() / 255f), lineWidth.getValue(), outline.getValue(), box.getValue(), entry.getValue(), true);
                }
            }else {
                RenderUtil.renderBox(pos, boxColor.getValue(), 1);
            }
        }
    }

    void dologic() {
        if(logic.getValue().equalsIgnoreCase("BREAKPLACE")) {
            logic();
            gloop();
        } else if(logic.getValue().equalsIgnoreCase("PLACEBREAK")) {
            gloop();
            logic();
        }
    }

    void logic() {
        final EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        if (crystal != null && mc.player.getDistance(crystal) <= range.getValue()) {
            if (ak47.getValue()) {
                crystal.setDead();
            }

            if (breakTimer.passedMs(hitDelay.getValue().intValue())) {

                if (predict.getValue()) {
                    final CPacketUseEntity attackPacket = new CPacketUseEntity();
                    mc.player.connection.sendPacket((Packet)attackPacket);
                }

                float[] arrf = RotationUtil.getRotations(crystal);
                if(rotate.getValue() && breakRotate.getValue()) {
                    lookAtCrystal(crystal);
                }
                mc.playerController.attackEntity(mc.player, crystal);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                breakTimer.reset();
            }
        }
    }

    void gloop() {
        int crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL
                ? mc.player.inventory.currentItem
                : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }

        boolean offhand = false;
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        } else if (crystalSlot == -1) {
            return;
        }
        double dmg = .5;
        mainhand = (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL);
        offhand = (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL);
        final List<EntityPlayer> entities = mc.world.playerEntities.stream()
                .filter(entityPlayer -> entityPlayer != mc.player && !Safepoint.friendInitializer.isFriend(entityPlayer.getName()))
                .collect(Collectors.toList());
        if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
            if (autoSwitch.getValue()) {
                mc.player.inventory.currentItem = crystalSlot;
            }
            return;
        }
        for (EntityPlayer entity2 : entities) {
            if (entity2.getHealth() <= 0.0f || mc.player.getDistance(entity2) > enemyRange.getValue())
                continue;
            for (final BlockPos blockPos : possiblePlacePositions((float) placeRange.getValue(), true)) {
                final double d = CrystalUtils.calcDmg(blockPos, entity2);
                final double self = CrystalUtils.calcDmg(blockPos, mc.player);
                if (d < minDmg.getValue()
                        && entity2.getHealth() + entity2.getAbsorptionAmount() > faceplace.getValue()
                        || maxSelfDmg.getValue() <= self || d <= dmg)
                    continue;
                dmg = d;
                pos = blockPos;
                target2 = entity2;
            }
        }

        if (dmg == .5) {
            render = null;
            return;
        }

        if (place.getValue()) {
            if (offhand || mainhand) {
                render = pos;
                if(rotate.getValue() && placeRotate.getValue()) {
                    lookAtPos(pos, EnumFacing.UP);
                }
                CrystalUtils.placeCrystalOnBlock(pos, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                damageString = String.valueOf(String.format("%.1f", dmg));
            }
        }
        if (fade.getValue())
            possesToFade.put(pos, startAlpha.getValue());
    }

    public List<BlockPos> possiblePlacePositions(float placeRange, boolean specialEntityCheck) {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(CrystalUtils.getSphere(CrystalUtils.getPlayerPos(), placeRange, (int) placeRange, false, true, 0).stream()
                .filter(pos -> newPlacement.getValue() ? canPlaceCrystal(pos, true) : ableToPlace(pos)).collect(Collectors.toList()));
        return positions;
    }

    public void onDisable() {
        render = null;
        target2 = null;
    }

    private float getRolledHeight(float offset) {
        double s = (System.currentTimeMillis() * pulseSpeed.getValue()) + (offset * rollingWidth.getValue() * 100.0f);
        s %= 300.0;
        s = (150.0f * Math.sin(((s - 75.0f) * Math.PI) / 150.0f)) + 150.0f;
        return pulseMax.getValue() + ((float)s * ((pulseMin.getValue() - pulseMax.getValue()) / 300.0f));
    }

    public boolean ableToPlace(BlockPos position) {
        Block placeBlock = mc.world.getBlockState(position).getBlock();

        if (!placeBlock.equals(Blocks.BEDROCK) && !placeBlock.equals(Blocks.OBSIDIAN)) {
            return false;
        }

        BlockPos nativePosition = position.up();
        BlockPos updatedPosition = nativePosition.up();

        Block nativeBlock = mc.world.getBlockState(nativePosition).getBlock();
        if (!nativeBlock.equals(Blocks.AIR) && !nativeBlock.equals(Blocks.FIRE)) {
            return false;
        }

        Block updatedBlock = mc.world.getBlockState(updatedPosition).getBlock();
        if (!updatedBlock.equals(Blocks.AIR) && !updatedBlock.equals(Blocks.FIRE)) {
            return false;
        }

        int unsafeEntities = 0;

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
                nativePosition.getX(), position.getY(), nativePosition.getZ(), nativePosition.getX() + 1, nativePosition.getY() + 2.0, nativePosition.getZ() + 1
        ))) {

            if (entity == null || entity.isDead || deadCrystals.contains(entity.getEntityId())) {
                continue;
            }

            if (entity instanceof EntityXPOrb) {
                continue;
            }
            if (entity instanceof EntityEnderCrystal) {

                if (attackedCrystals.containsKey(entity.getEntityId()) && entity.ticksExisted < 20) {
                    continue;
                }

                double localDamage = CrystalUtils.getDamageFromExplosion(mc.player, entity.getPositionVector(), false);

                double idealDamage = 0;

                for (Entity target : new ArrayList<>(mc.world.loadedEntityList)) {

                    if (target == null || target.equals(mc.player) || target.getEntityId() < 0 || isDead(target) || Safepoint.friendInitializer.isFriend(entity.getName())) {
                        continue;
                    }

                    if (target instanceof EntityEnderCrystal) {
                        continue;
                    }

                    if (target.isBeingRidden() && target.getPassengers().contains(mc.player)) {
                        continue;
                    }

                    if (target instanceof EntityPlayer) {
                        continue;
                    }

                    double entityRange = mc.player.getDistance(target);

                    if (entityRange > placeRange.getValue()) {
                        continue;
                    }

                    double targetDamage = CrystalUtils.getDamageFromExplosion(target, entity.getPositionVector(), false);
                    double safetyIndex = 1;

                    if (canTakeDamage()) {

                        double health = mc.player.getHealth();

                        if (localDamage + 0.5 > health) {
                            safetyIndex = -9999;
                        }
                        double efficiency = targetDamage - localDamage;

                        if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                            efficiency = 0;
                        }

                        safetyIndex = efficiency;

                    }

                    if (safetyIndex < 0) {
                        continue;
                    }

                    if (targetDamage > idealDamage) {
                        idealDamage = targetDamage;
                    }
                }

                if (idealDamage > 2.0) {
                    continue;
                }
            }

            unsafeEntities++;
        }
        return unsafeEntities <= 0;
    }

    private boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            if (!this.newPlacement.getValue()) {
                if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
                }
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            } else {
                if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
                }
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    boolean isLiving(Entity entity) {
        return entity instanceof EntityLivingBase;
    }

    boolean isAlive(Entity entity) {
        return isLiving(entity) && !entity.isDead && ((EntityLivingBase) entity).getHealth() >= 0.0f;
    }

    boolean isDead(Entity entity) {
        return !isAlive(entity);
    }

    boolean isMoving() {
        return (double) mc.player.moveForward != 0.0 || (double) mc.player.moveStrafing != 0.0;
    }

    boolean canTakeDamage() {
        return !mc.player.capabilities.isCreativeMode;
    }

    private void packetBreak(EntityPlayerSP player, SPacketSpawnObject packet) {
        if ((1000 / hitDelay.getValue()) > 0 && !breakTimer.passedMs((long)(1000 / hitDelay.getValue()))) return;
        double distance = player.getDistance(packet.getX(), packet.getY(), packet.getZ());
        if (distance > range.getValue()) return;
        Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
        if (wall.getValue() && distance > walls.getValue())
            return;
        if (!canHitCrystal(pos)) return;
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        CrystalUtils.setEntityId(attackPacket, packet.getEntityID());
        CrystalUtils.setAction(attackPacket, CPacketUseEntity.Action.ATTACK);
        mc.player.connection.sendPacket(attackPacket);
        breakTimer.reset();
    }

    private boolean canHitCrystal(Vec3d crystal) {
        if (mc.player.getDistance(crystal.x, crystal.y, crystal.z) > range.getValue()) return false;
        else return true;
    }

}