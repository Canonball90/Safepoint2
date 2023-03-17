package safepoint.two.module.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.EventModelPlayerRender;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.ColorSetting;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.core.settings.impl.IntegerSetting;

import java.awt.*;

@ModuleInfo(name = "Chams", description = "", category = Module.Category.Visual)
public class Chams extends Module {

    private boolean selfBdown = false;
    private boolean friendBdown = false;
    private boolean enemyBdown = false;

    public BooleanSetting xyz = new BooleanSetting("XYZ", true, this);
    public BooleanSetting uuuu = new BooleanSetting("IDK", true, this);

    public BooleanSetting self = new BooleanSetting("Self", true, this);
    public ColorSetting selfColor = new ColorSetting("SelfColor", new Color(0,255,0), this,v -> self.getValue());
    public IntegerSetting selfA = new IntegerSetting("SelfAlpha", 150, 0, 255, this, v -> self.getValue());
    public BooleanSetting selfBL = new BooleanSetting("SelfBlink", true, this,v -> self.getValue());
    public FloatSetting selfBS = new FloatSetting("SelfBlinkSpeed", 1.f, 0.1f, 10f, this,v -> self.getValue());
    public BooleanSetting selfNoAr = new BooleanSetting("SelfNoArmor", false, this,v -> self.getValue());
    public BooleanSetting selfFGl = new BooleanSetting("SelfForceGlint", false, this,v -> self.getValue());
    public BooleanSetting selfLTH = new BooleanSetting("SelfLightning", false, this,v -> self.getValue());
    public BooleanSetting selfANG = new BooleanSetting("SelfAngel", false, this,v -> self.getValue());

    public BooleanSetting friend = new BooleanSetting("Friend", true, this);
    public ColorSetting friendColor = new ColorSetting("FriendColor", new Color(0, 51, 255), this,v -> friend.getValue());
    public IntegerSetting friendA = new IntegerSetting("FriendAlpha", 150, 0, 255, this, v -> self.getValue());
    public BooleanSetting friendBL = new BooleanSetting("FriendBlink", true, this,v -> friend.getValue());
    public FloatSetting friendBS = new FloatSetting("FriendBlinkSpeed", 1.f, 0.1f, 10f, this,v -> friend.getValue());
    public BooleanSetting friendNoAr = new BooleanSetting("FriendNoArmor", false, this,v -> friend.getValue());
    public BooleanSetting friendFGl = new BooleanSetting("FriendForceGlint", false, this,v -> friend.getValue());

    public BooleanSetting enemy = new BooleanSetting("Enemy", true, this);
    public ColorSetting enemyColor = new ColorSetting("EnemyColor", new Color(0, 51, 255), this,v -> enemy.getValue());
    public IntegerSetting enemyA = new IntegerSetting("EnemyAlpha", 150, 0, 255, this, v -> self.getValue());
    public BooleanSetting enemyBL = new BooleanSetting("EnemyBlink", true, this,v -> enemy.getValue());
    public FloatSetting enemyBS = new FloatSetting("EnemyBlinkSpeed", 1.f, 0.1f, 10f, this,v -> enemy.getValue());
    public BooleanSetting enemyNoAr = new BooleanSetting("EnemyNoArmor", false, this,v -> enemy.getValue());
    public BooleanSetting enemyFGl = new BooleanSetting("EnemyForceGlint", false, this,v -> enemy.getValue());

    public EntityPlayer lastPlayer = null;

    @Override
    public void onWorldRender() {
        super.onWorldRender();
        if(!this.isEnabled())
            return;

        // Self
        if(this.selfBL.getValue()){
            if(this.selfBdown){
                this.selfA.setValue((int)(this.selfA.getValue() - this.friendBS.getValue()));
                if(this.selfA.getValue() < 2)
                    this.selfBdown = false;
            }else{
                this.selfA.setValue((int)(this.selfA.getValue() + this.selfBS.getValue() * 2.f));
                if(this.selfA.getValue() > 253)
                    this.selfBdown = true;
            }
        }

        // Friend
        if(this.friendBL.getValue()){
            if(this.friendBdown){
                this.friendA.setValue((int)(this.friendA.getValue() - this.friendBS.getValue()));
                if(this.friendA.getValue() < 2)
                    this.friendBdown = false;
            }else{
                this.friendA.setValue((int)(this.friendA.getValue() + this.friendBS.getValue()));
                if(this.friendA.getValue() > 253)
                    this.friendBdown = true;
            }
        }

        // Enemy
        if(this.enemyBL.getValue()){
            if(this.enemyBdown){
                this.enemyA.setValue((int)(this.enemyA.getValue() - this.enemyBS.getValue()));
                if(this.enemyA.getValue() < 2)
                    this.enemyBdown = false;
            }else{
                this.enemyA.setValue((int)(this.enemyA.getValue() + this.enemyBS.getValue()));
                if(this.enemyA.getValue() > 253)
                    this.enemyBdown = true;
            }
        }
    }

//    @SubscribeEvent
//    public void onRenderPlayerEvent(EventModelPlayerRender event){
//        if(!(event.entity instanceof EntityPlayer))
//            return;
//
//        EntityPlayer e = (EntityPlayer)event.entity;
//
//        Chams chamsModule = (Chams) Safepoint.moduleInitializer.find(Chams.class);
//
//        if(!chamsModule.isEnabled() || e == null)
//            return;
//
//        if(Safepoint.friendInitializer.isFriend(e.getName()) && chamsModule.friend.getValue() ||
//                !Safepoint.friendInitializer.isFriend(e.getName()) && chamsModule.enemy.getValue() ||
//                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){
//
//            GlStateManager.enableAlpha();
//            GlStateManager.enableBlend();
//            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            GlStateManager.depthMask(false);
//
//            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
//                GL11.glColor4f(selfColor.getValue().getRed(),selfColor.getValue().getGreen(),selfColor.getValue().getBlue(),selfA.getValue());
//            } else if(Safepoint.friendInitializer.isFriend(e.getName())){
//                //friend settings
//                GL11.glColor4f(friendColor.getValue().getRed(),friendColor.getValue().getGreen(),friendColor.getValue().getBlue(),friendA.getValue());
//            } else if(!Safepoint.friendInitializer.isFriend(e.getName())){
//                //enemy settings
//                GL11.glColor4f(enemyColor.getValue().getRed(),enemyColor.getValue().getGreen(),enemyColor.getValue().getBlue(),enemyA.getValue());
//            }
//        }
//    }

    @SubscribeEvent
    public void onRenderPre(EventModelPlayerRender event) {
        if(event.type == 0) {
            doChamPre();
        }
    }

    @SubscribeEvent
    public void onRenderPost(EventModelPlayerRender event) {
        if(event.type == 1) {
            doChamPost();
        }
    }


    @SubscribeEvent
    public void onPlayerModel(final EventModelPlayerRender event) {
        final Color ce = enemyColor.getValue();
        final Color cf = friendColor.getValue();
        if (xyz.getValue()) {
            if (event.type == 0) {
                GL11.glPushMatrix();
                GL11.glEnable(32823);
                GL11.glPolygonOffset(1.0f, -1.0E7f);
                GL11.glPushAttrib(1048575);
                GL11.glPolygonMode(1028, 6913);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glDisable(2929);
                GL11.glEnable(2848);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                if(uuuu.getValue()) {
                    if (enemy.getValue() && !Safepoint.friendInitializer.isFriend(event.entity.getName())) {
                        GL11.glColor4f(ce.getRed() / 255.0f, ce.getGreen() / 255.0f, ce.getBlue() / 255.0f, ce.getAlpha() / 255.0f / 2.0f);
                    } else if (friend.getValue() && Safepoint.friendInitializer.isFriend(event.entity.getName())) {
                        GL11.glColor4f(cf.getRed() / 255.0f, cf.getGreen() / 255.0f, cf.getBlue() / 255.0f, cf.getAlpha() / 255.0f / 2.0f);
                    }
                }
            } else if (event.type == 1) {
                GL11.glPopAttrib();
                GL11.glPolygonOffset(1.0f, 1.0E7f);
                GL11.glDisable(32823);
                GL11.glPopMatrix();
            }
        }
    }


    private void doChamPre() {
        GlStateManager.pushMatrix();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

        // disable shadows and outline
        mc.getRenderManager().setRenderShadow(false);
        mc.getRenderManager().setRenderOutlines(false);

        // making entity visible behind blocks
        GL11.glDepthRange(0.0, 0.1);

//        if (renderMode.getMode().equals("Color")) {
//            GL11.glDisable(GL11.GL_TEXTURE_2D);
//            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
//            GlStateManager.color(r.getValue() / 255f, g.getValue() / 255f, b.getValue() / 255f, a.getValue() / 255f);
//        }
        GlStateManager.popMatrix();
    }

    private void doChamPost() {
        GlStateManager.pushMatrix();

        // re enabling stuff
        mc.getRenderManager().setRenderShadow(mc.getRenderManager().isRenderShadow());

        // make un visible
        GL11.glDepthRange(0.0, 1.0);

        // disable color model (if mode is color)
//        if (renderMode.getMode().equals("Color")) {
//            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
//            GlStateManager.color(1f, 1f, 1f, 1f);
//            GL11.glEnable(GL11.GL_TEXTURE_2D);
//        }

        // pop matrix
        GlStateManager.popMatrix();
    }

}
