package safepoint.two.module.combat;

import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.RootEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.core.settings.impl.IntegerSetting;
import safepoint.two.utils.math.Timer;

import java.util.*;
import java.util.stream.Collectors;

@ModuleInfo(name = "AutoTotem", description = "", category = Module.Category.Combat)
public class AutoTotem extends Module {

    EnumSetting mode = new EnumSetting("Mode", "Strict", Arrays.asList("Strict", "Semi"), this);
    BooleanSetting noSpring = new BooleanSetting("NoSprint", false, this);
    BooleanSetting totem = new BooleanSetting("Totem", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting hard = new BooleanSetting("Hard", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting crystal = new BooleanSetting("Crystal", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting rightClick = new BooleanSetting("Click", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting totemOnElytra = new BooleanSetting("OnElytra", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting fallCheck = new BooleanSetting("FallCheck", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    IntegerSetting fallDist = new IntegerSetting("FallDist", 10, 0, 50, this,v -> fallCheck.getValue() && mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting extraSafe = new BooleanSetting("ExtraSafe", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting gapple = new BooleanSetting("Gapple", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting clearAfter = new BooleanSetting("Clear", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    IntegerSetting crystalRange = new IntegerSetting("Range", 10, 0, 15, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    IntegerSetting delay = new IntegerSetting("Delay", 0, 0, 5, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    IntegerSetting totemHealthThreshold = new IntegerSetting("TotemHealth", 12, 0, 36, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    BooleanSetting hotbarTotem = new BooleanSetting("HotBar", false, this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    EnumSetting defaultItem = new EnumSetting("DefaultItems", "Totem", Arrays.asList("Totem","Crystal","Shield","Air","Gapple"), this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    EnumSetting crystalCheck = new EnumSetting("CrystalCheck", "Damage", Arrays.asList("Damage","Range","None"), this,v -> mode.getValue().equalsIgnoreCase("Semi"));
    private final Queue<Integer> clickQueue = new LinkedList<>();
    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onRoot(RootEvent event){
        doNullCheck();
        if (mode.getValue().equalsIgnoreCase("Strict")) {
            if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) return;
            if (getItemSlot(Items.TOTEM_OF_UNDYING, false) != -1) {
                if (mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
                    moveToOffhand(getItemSlot(Items.TOTEM_OF_UNDYING, false));
                }
            } else if (getItemSlot(Items.GOLDEN_APPLE, true) != -1) {
                if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
                    moveToOffhand(getItemSlot(Items.GOLDEN_APPLE, true));
                }
            }
        }
        if(mode.getValue().equalsIgnoreCase("Semi")){

            if (!(mc.currentScreen instanceof GuiContainer)) {
                if (!clickQueue.isEmpty()) {
                    if (!timer.passedMs((long) (delay.getValue() * 100))) return;
                    int slot = clickQueue.poll();
                    try {
                        timer.reset();
                        if (noSpring.getValue())
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                    if (!mc.player.inventory.getItemStack().isEmpty()) {
                        for (int index = 44; index >= 9; index--) {
                            if (mc.player.inventoryContainer.getSlot(index).getStack().isEmpty()) {
                                if (noSpring.getValue())
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                                mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                                return;
                            }
                        }
                    }

                    if (totem.getValue()) {
                        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= totemHealthThreshold.getValue()
                                || (totemOnElytra.getValue() && mc.player.isElytraFlying())
                                || (fallCheck.getValue() && mc.player.fallDistance >= fallDist.getValue() && !mc.player.isElytraFlying())) {

                            putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                            return;
                        } else if (crystalCheck.getValue().equalsIgnoreCase("Range")) {
                            EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream()
                                    .filter(e -> (e instanceof EntityEnderCrystal && mc.player.getDistance(e) <= crystalRange.getValue()))
                                    .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                                    .orElse(null);

                            if (crystal != null) {
                                putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                                return;
                            }
                        } else if (crystalCheck.getValue().equalsIgnoreCase("Damage")) {
                            float damage = 0.0f;

                            List<Entity> crystalsInRange = mc.world.loadedEntityList.stream()
                                    .filter(e -> e instanceof EntityEnderCrystal)
                                    .filter(e -> mc.player.getDistance(e) <= crystalRange.getValue())
                                    .collect(Collectors.toList());

                            for (Entity entity : crystalsInRange) {
                                damage += calculateDamage((EntityEnderCrystal) entity, mc.player);
                            }

                            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() - damage <= totemHealthThreshold.getValue()) {
                                putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                                return;
                            }
                        }

                        if (extraSafe.getValue()) {
                            if (crystalCheck()) {
                                putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                                return;
                            }
                        }
                    }

                    if (gapple.getValue() && isSword(mc.player.getHeldItemMainhand().getItem())) {
                        if (rightClick.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
                            if (clearAfter.getValue()) {
                                putItemIntoOffhand(getDeafaultItem());
                            }
                            return;
                        }
                        putItemIntoOffhand(Items.GOLDEN_APPLE);
                        return;
                    }

                    if (crystal.getValue()) {
                        if (Safepoint.moduleInitializer.find(AutoCrystal.class).isEnabled()) {
                            putItemIntoOffhand(Items.END_CRYSTAL);
                            return;
                        } else if (clearAfter.getValue()) {
                            putItemIntoOffhand(getDeafaultItem());
                            return;
                        }
                    }
                    if (hard.getValue()) {
                        if ((getDeafaultItem() == Items.SHIELD && mc.player.getCooldownTracker().hasCooldown(Items.SHIELD)) || (getDeafaultItem() == Items.SHIELD && getItemSlot(Items.SHIELD, false) == -1 && mc.player.getHeldItemOffhand().getItem() != Items.SHIELD)) {
                            putItemIntoOffhand(Items.GOLDEN_APPLE);
                        } else {
                            putItemIntoOffhand(getDeafaultItem());
                        }
                    }
                }
            }
        }
    }

    private boolean isSword(Item item) {
        return item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD || item == Items.STONE_SWORD || item == Items.WOODEN_SWORD;
    }

    public void moveToOffhand(int from) {
        if (!timer.passedMs(100)) return;
        if (from == -1) return;
        if (noSpring.getValue())
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
        mc.playerController.updateController();
        timer.reset();
    }


    public static int getItemSlot(Item item, boolean gappleCheck) {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStackInSlot = mc.player.inventory.getStackInSlot(i);
            if (!gappleCheck) {
                if (item == itemStackInSlot.getItem()) {
                    if (i < 9) i += 36;
                    return i;
                }
            } else {
                if (item == itemStackInSlot.getItem() && (!item.getRarity(itemStackInSlot).equals(EnumRarity.RARE) || (noGapples()))) {
                    if (i < 9) i += 36;
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean noGapples() {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStackInSlot = mc.player.inventory.getStackInSlot(i);
            if (Items.GOLDEN_APPLE == itemStackInSlot.getItem() && !Items.GOLDEN_APPLE.getRarity(itemStackInSlot).equals(EnumRarity.RARE)) {
                return false;
            }
        }
        return true;
    }

    public static List<BlockPos> getSphere(final BlockPos blockPos, final float n, final int n2, final boolean b,
                                           final boolean b2, final int n3) {
        final ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        final int x = blockPos.getX();
        final int y = blockPos.getY();
        final int z = blockPos.getZ();
        for (int n4 = x - (int) n; n4 <= x + n; ++n4) {
            for (int n5 = z - (int) n; n5 <= z + n; ++n5) {
                for (int n6 = b2 ? (y - (int) n) : y; n6 < (b2 ? (y + n) : ((float) (y + n2))); ++n6) {
                    final double n7 = (x - n4) * (x - n4) + (z - n5) * (z - n5) + (b2 ? ((y - n6) * (y - n6)) : 0);
                    if (n7 < n * n && (!b || n7 >= (n - 1.0f) * (n - 1.0f))) {
                        list.add(new BlockPos(n4, n6 + n3, n5));
                    }
                }
            }
        }
        return list;
    }

    public Item getDeafaultItem(){
        switch (defaultItem.getValue()){
            case "Totem":
                return Items.TOTEM_OF_UNDYING;
            case "Gapple":
                return Items.GOLDEN_APPLE;
            case "Crystal":
                return Items.END_CRYSTAL;
            case "Shield":
                return Items.SHIELD;
        }return Items.AIR;
    }

    private void putItemIntoOffhand(Item item) {
        if (mc.player.getHeldItemOffhand().getItem() == item) return;
        int slot = getItemSlot(item, false);
        if (hotbarTotem.getValue() && item == Items.TOTEM_OF_UNDYING) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.mainInventory.get(i);
                if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    if (mc.player.inventory.currentItem != i) {
                        mc.player.inventory.currentItem = i;
                    }
                    return;
                }
            }
        }
        if (slot != -1) {
            if (delay.getValue() > 0F) {
                if (timer.passedMs((long) (delay.getValue() * 100))) {
                    if (noSpring.getValue())
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
                    timer.reset();
                } else {
                    clickQueue.add(slot < 9 ? slot + 36 : slot);
                }

                clickQueue.add(45);
                clickQueue.add(slot < 9 ? slot + 36 : slot);
            } else {
                timer.reset();
                if (noSpring.getValue())
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    private boolean crystalCheck() {
        float cumDmg = 0;
        ArrayList<Float> damageValues = new ArrayList<>();
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(1, 0, 0)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(-1, 0, 0)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(0, 0, 1)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(0, 0, -1)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition()));
        for (float damage : damageValues) {
            cumDmg += damage;
            if ((((mc.player.getHealth() + mc.player.getAbsorptionAmount())) - damage) <= totemHealthThreshold.getValue()) {
                return true;
            }
        }
        return (((mc.player.getHealth() + mc.player.getAbsorptionAmount())) - cumDmg) <= totemHealthThreshold.getValue();
    }

    private float calculateDamageAABB(BlockPos pos) {
        List<Entity> crystalsInAABB = mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream()
                .filter(e -> e instanceof EntityEnderCrystal)
                .collect(Collectors.toList());
        float totalDamage = 0;
        for (Entity crystal : crystalsInAABB) {
            totalDamage += calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player);
        }
        return totalDamage;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0F;
        double distancedsize;
        Vec3d entityPosVec = getEntityPosVec(entity, 3);
        distancedsize = entityPosVec.distanceTo(new Vec3d(posX, posY, posZ)) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0D;
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox().offset(getMotionVec(entity, 3)));
        } catch (Exception ignored) {

        }
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald = 1;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, mc.player, posX, posY, posZ, 6F, false, true));
        }
        return (float) finald;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static Vec3d getEntityPosVec(Entity entity, int ticks) {
        return entity.getPositionVector().add(getMotionVec(entity, ticks));
    }

    public static Vec3d getMotionVec(Entity entity, int ticks) {
        double dX = entity.posX - entity.prevPosX;
        double dZ = entity.posZ - entity.prevPosZ;
        double entityMotionPosX = 0;
        double entityMotionPosZ = 0;

        for (int i = 1; i <= ticks; i++) {
            if (mc.world.getBlockState(new BlockPos(entity.posX + dX * i, entity.posY, entity.posZ + dZ * i)).getBlock() instanceof BlockAir) {
                entityMotionPosX = dX * i;
                entityMotionPosZ = dZ * i;
            } else {
                break;
            }
        }

        return new Vec3d(entityMotionPosX, 0, entityMotionPosZ);
    }

    public static float getBlastReduction(EntityLivingBase entity, float damageInput, Explosion explosion) {
        float damage = damageInput;
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            } catch (Exception ignored) {

            }
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage = damage * (1.0F - f / 25.0F);

            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage = damage - (damage / 4);
            }

            damage = Math.max(damage, 0.0F);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }


    private enum Default {
        TOTEM(Items.TOTEM_OF_UNDYING, "Totem"),
        CRYSTAL(Items.END_CRYSTAL, "Crystal"),
        GAPPLE(Items.GOLDEN_APPLE, "Gapple"),
        AIR(Items.AIR, "Air"),
        SHIELD(Items.SHIELD, "Shield");

        public Item item;
        public String name;

        Default(Item item, String name) {
            this.item = item;
            this.name = name;
        }
    }
}
