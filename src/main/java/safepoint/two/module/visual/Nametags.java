package safepoint.two.module.visual;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.*;
import safepoint.two.utils.core.MathUtil;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import scala.actors.UncaughtException;
import scala.actors.threadpool.helpers.ThreadHelpers;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ModuleInfo(name = "NameTags", description = "", category = Module.Category.Visual)
public class Nametags extends Module {

    public FloatSetting size = new FloatSetting("Size", 2f, 0.1f, 10f, this);
    public FloatSetting hight = new FloatSetting("Hight", 0f, -10f, 10f, this);
    public BooleanSetting customPos = new BooleanSetting("CustomPos", false, this);
    public FloatSetting x = new FloatSetting("X", 0f, -10f, 10f, this,v -> customPos.getValue());
    public FloatSetting y = new FloatSetting("Y", 0f, -10f, 10f, this,v -> customPos.getValue());
    public FloatSetting z = new FloatSetting("Z", 0f, -10f, 10f, this,v -> customPos.getValue());

    public ParentSetting boxParent = new ParentSetting("Box", false, this);
    public BooleanSetting rounded = new BooleanSetting("Rounded", false, this).setParent(boxParent);
    public DoubleSetting radius = new DoubleSetting("Radius", 4.0, 0.1, 5.0, this, v -> rounded.getValue()).setParent(boxParent);
    public FloatSetting outlineSize = new FloatSetting("OutlineSize", 1.2f, 0.1f, 5.0f, this).setParent(boxParent);
    public FloatSetting height = new FloatSetting("Height", 0f, -10f, 10f, this).setParent(boxParent);
    public FloatSetting width = new FloatSetting("Width", 0f, -10f, 10f, this).setParent(boxParent);

    public ParentSetting other = new ParentSetting("Other", false, this);
    public BooleanSetting health = new BooleanSetting("HP", true, this).setParent(other);
    public BooleanSetting items = new BooleanSetting("Items", true, this).setParent(other);
    public BooleanSetting armor = new BooleanSetting("Armor", true, this).setParent(other);
    public BooleanSetting entchantments = new BooleanSetting("Entchantments", false, this).setParent(other);
    public BooleanSetting ping = new BooleanSetting("Ping", true, this).setParent(other);
    public BooleanSetting gameMode = new BooleanSetting("GameMode", false, this).setParent(other);
    public BooleanSetting heart = new BooleanSetting("Heart", false, this).setParent(other);
    public BooleanSetting mutliThread = new BooleanSetting("Threaded", false, this).setParent(other);
    public BooleanSetting outline = new BooleanSetting("Outline", false, this).setParent(other);
    public BooleanSetting shadowedText = new BooleanSetting("ShadowedText", false, this).setParent(other);

    public ParentSetting colour = new ParentSetting("Colors", false, this);
    public ColorSetting color = new ColorSetting("Color", new Color(255, 2, 2), this).setParent(colour);
    public ColorSetting BackColor = new ColorSetting("BackGroundColor", new Color(7, 5, 5, 152), this).setParent(colour);
    public BooleanSetting crouchColor = new BooleanSetting("CrouchColor", false, this).setParent(colour);
    public ColorSetting Ccolor = new ColorSetting("CrouchColour", new Color(2, 141, 255), this, v -> crouchColor.getValue()).setParent(colour);
    public BooleanSetting rainbowOutline = new BooleanSetting("RainbowOutline", false, this).setParent(colour);

    public BooleanSetting crouch = new BooleanSetting("Crouch", false, this);
    public FloatSetting crouchSize = new FloatSetting("CrouchSize", 1.2f, 0.1f, 5.0f, this, v -> crouch.getValue());
    public static Nametags INSTANCE;

    public Nametags() {
        INSTANCE = this;
    }

    public static Nametags getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Nametags();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (final EntityPlayer p : mc.world.playerEntities) {
            if (p != mc.getRenderViewEntity() && p.isEntityAlive()) {
                final double pX;
                final double pY;
                final double pZ;

                if(customPos.getValue()){
                    pX = p.lastTickPosX + (p.posX - p.lastTickPosX) * (Nametags.mc).timer.renderPartialTicks - (Nametags.mc.getRenderManager()).renderPosX + x.getValue();
                    pY = p.lastTickPosY + (p.posY - p.lastTickPosY) * (Nametags.mc).timer.renderPartialTicks - (Nametags.mc.getRenderManager()).renderPosY - (crouch.getValue() && isCrouching(p) ? crouchSize.getValue() : 0) + hight.getValue() + y.getValue();
                    pZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * (Nametags.mc).timer.renderPartialTicks - (Nametags.mc.getRenderManager()).renderPosZ + z.getValue();
                }else {
                    pX = p.lastTickPosX + (p.posX - p.lastTickPosX) * (Nametags.mc).timer.renderPartialTicks - (Nametags.mc.getRenderManager()).renderPosX;
                    pY = p.lastTickPosY + (p.posY - p.lastTickPosY) * (Nametags.mc).timer.renderPartialTicks - (Nametags.mc.getRenderManager()).renderPosY - (crouch.getValue() && isCrouching(p) ? crouchSize.getValue() : 0) + hight.getValue();
                    pZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * (Nametags.mc).timer.renderPartialTicks - (Nametags.mc.getRenderManager()).renderPosZ;
               }
                if (p.getName().startsWith("Body #")) {
                    continue;
                }
                if (mutliThread.getValue()) {
                    try {
                        run(() -> renderNametag(p, pX, pY, pZ));
                    } catch (Exception e) {
                        renderNametag(p, pX, pY, pZ);
                        e.printStackTrace();
                    }
                }else {
                    this.renderNametag(p, pX, pY, pZ);
                }
            }
        }
    }

    private void renderNametag(final EntityPlayer player, final double x, final double y, final double z) {
        GL11.glPushMatrix();
        String name = (Safepoint.friendInitializer.isFriend(player.getName()) ? ChatFormatting.AQUA : ChatFormatting.WHITE) + player.getName();
        if (gameMode.getValue()) {
            name = name + "" + this.getGMText(player) + "§f";
        }
        if (ping.getValue()) {
            name = name + " " + this.getPing(player) + "ms";
        }
        if (health.getValue()) {
            name = name + " §r" + MathHelper.ceil(player.getHealth() + player.getAbsorptionAmount());
        }
        final float var14 = 0.016666668f * this.getNametagSize(player);
        GL11.glTranslated((float) x, (float) y + 2.5, (float) z);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(-var14, -var14, var14);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GL11.glDisable(2929);
        Color c;
        if (rainbowOutline.getValue()) {
            c = new Color(255, 0, 0);
        } else if (crouchColor.getValue() && isCrouching(player)) {
            c = Ccolor.getValue();
        } else {
            c = color.getValue();
        }
        final int width = RenderUtil.getStringWidth(name) / 2;

        if (rounded.getValue()) {
            RenderUtil.roundedRect(-width - 3 - this.width.getValue(), 8.0f - this.height.getValue(), width + 52 + this.width.getValue(), 13 + this.height.getValue(), radius.getValue(), c);
        } else {
            RenderUtil.drawBorderedRect(-width - 3 - this.width.getValue(), 8.0f - this.height.getValue(), width + 2 + this.width.getValue(), 20.0f + this.height.getValue(), outlineSize.getValue(), BackColor.getValue().getRGB(), (outline.getValue() ? c.getRGB() : 1962934272));
        }

        if(shadowedText.getValue()) {
            mc.fontRenderer.drawStringWithShadow(name, (float) (-width), 10.0f, -1);
        }else{
            mc.fontRenderer.drawString(name, (-width), 10, -1);
        }

        int xOffset = 0;

        if (armor.getValue()) {
            for (final ItemStack armourStack : player.inventory.armorInventory) {
                if (armourStack != null) {
                    xOffset -= 8;
                }
            }
        }

        player.getHeldItemMainhand();
        xOffset -= 8;
        final ItemStack renderStack = player.getHeldItemMainhand().copy();
        this.renderItem(renderStack, xOffset, -10);
        xOffset += 16;

        if (armor.getValue()) {
            for (int index = 3; index >= 0; --index) {
                final ItemStack armourStack2 = player.inventory.armorInventory.get(index);
                final ItemStack renderStack2 = armourStack2.copy();
                this.renderItem(renderStack2, xOffset, -10);
                xOffset += 16;
            }
        }
        player.getHeldItemOffhand();
        xOffset += 0;
        final ItemStack renderOffhand = player.getHeldItemOffhand().copy();
        this.renderItem(renderOffhand, xOffset, -10);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private float getNametagSize(final EntityLivingBase player) {
        final ScaledResolution scaledRes = new ScaledResolution(Nametags.mc);
        final double twoDscale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 0.0 + size.getValue());
        return (float) twoDscale + Nametags.mc.player.getDistance(player) / 5.6f;
    }

    public String getGMText(final EntityPlayer player) {
        if (player.isCreative()) {
            return " [C]";
        }
        if (player.isSpectator()) {
            return " [I]";
        }
        if (!player.isAllowEdit() && !player.isSpectator()) {
            return " [A]";
        }
        if (!player.isCreative() && !player.isSpectator() && player.isAllowEdit()) {
            return " [S]";
        }
        return "";
    }

    private void renderItem(final ItemStack stack, final int x, final int y) {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.clear(256);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -100.0f;
        GlStateManager.scale(1.0f, 1.0f, 0.01f);
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y / 2 - 12);
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y / 2 - 12);
        mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.scale(1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.disableDepth();
        if (entchantments.getValue()) {
            this.renderEnchantText(stack, x, y - 18);
        }
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GL11.glPopMatrix();
    }

    public int getPing(final EntityPlayer player) {
        int ping = 0;
        try {
            ping = (int) MathUtil.clamp((float) Objects.requireNonNull(mc.getConnection()).getPlayerInfo(player.getUniqueID()).getResponseTime(), 1.0f, 300.0f);
        } catch (NullPointerException ex) {
        }
        return ping;
    }

    private void renderEnchantText(final ItemStack stack, final int x, final int y) {
        int encY = y - 24;
        int yCount = encY + 5;
        if (stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemTool) {
            final float green = (stack.getMaxDamage() - (float) stack.getItemDamage()) / stack.getMaxDamage();
            final float red = 1.0f - green;
            final int dmg = 100 - (int) (red * 100.0f);
            assert red <= 255;
            assert green <= 255;
            mc.fontRenderer.drawStringWithShadow(dmg + "%", x * 2 + 8, y + 26, new Color((int) (red * 255.0f), (int) (green * 255.0f), 0).getRGB());
        }
        final NBTTagList enchants = stack.getEnchantmentTagList();
        for (int index = 0; index < enchants.tagCount(); ++index) {
            final short id = enchants.getCompoundTagAt(index).getShort("id");
            final short level = enchants.getCompoundTagAt(index).getShort("lvl");
            final Enchantment enc = Enchantment.getEnchantmentByID(id);
            if (enc != null) {
                String encName = enc.isCurse() ? (TextFormatting.RED + enc.getTranslatedName(level).substring(11).substring(0, 1).toLowerCase()) : enc.getTranslatedName(level).substring(0, 1).toLowerCase();
                encName += level;
                GL11.glPushMatrix();
                GL11.glScalef(0.9f, 0.9f, 0.0f);
                mc.fontRenderer.drawStringWithShadow(encName, x * 2 + 13, yCount, -1);
                GL11.glScalef(1.0f, 1.0f, 1.0f);
                GL11.glPopMatrix();
                encY += 8;
                yCount -= 10;
            }
        }
    }

    boolean isCrouching(EntityPlayer p) {
        return p.isSneaking();
    }

    protected ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void run(Runnable command) {
        try {
            executorService.execute(command);
        } catch (Exception ignored) {
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
