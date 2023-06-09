package safepoint.two.guis.mainmenu;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.RenderTexture;
import safepoint.two.Safepoint;
import safepoint.two.utils.render.RenderUtil;

public class MainMenu
        extends GuiScreen {
    private final Button singlePlayerButton = new Button("Single", this.width / 2 - 75, this.height / 2, 75);
    private final Button multiPlayerButton;
    private final Button settingsButton;
    private final Button discordButton;
    private final Button quitButton;
    private final Button alts;
    ArrayList<Button> buttons = new ArrayList();

    public MainMenu() {
        this.buttons.add(this.singlePlayerButton);
        this.multiPlayerButton = new Button("Multi", this.width / 2 - 75, this.height / 2 + 25, 75);
        this.buttons.add(this.multiPlayerButton);
        this.settingsButton = new Button("Options", this.width / 2 - 125, this.height / 2, 75);
        this.buttons.add(this.settingsButton);
        this.quitButton = new Button("Quit", this.width / 2 - 125, this.height / 2 + 25, 75);
        this.buttons.add(this.quitButton);
        this.alts = new Button("ALTS", this.width / 2 - 75, this.height / 2, 75);
        this.buttons.add(this.alts);
        this.discordButton = new Button("Discord", this.width / 2 - 75, this.height / 2, 75);
        this.buttons.add(this.discordButton);
       // this.playMusic();
    }

    private void playMusic() {
        if (!mc.soundHandler.isSoundPlaying(Safepoint.soundInitializer.getMenuSong())) {
            mc.soundHandler.playSound(Safepoint.soundInitializer.getMenuSong());
        }
    }


    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        ScaledResolution sr = new ScaledResolution(this.mc);
        RenderUtil.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0,0,0).getRGB());
        RenderUtil.roundedRect(5,5,sr.getScaledWidth()/5,sr.getScaledHeight()/3, sr.getScaledWidth()/15, new Color(224, 29, 29));
        RenderUtil.roundedRect(sr.getScaledWidth()-sr.getScaledWidth()/5, sr.getScaledHeight()-sr.getScaledHeight()/3, sr.getScaledWidth()/5, sr.getScaledHeight()/3, sr.getScaledWidth()/15, new Color(127, 140, 255));
        //mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));

//        drawBlurredBackground(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 10, new Color(255,0,0).getRGB());

        //mc.fontRenderer.drawString("Safepoint", 4, sr.getScaledHeight() - mc.fontRenderer.FONT_HEIGHT, -1);
        drawLogo.drawString(5, "Safepoint+2", this.width / 10 - this.fontRenderer.getStringWidth("Safepoint+2") / 2, this.height / 20,-1);

        this.settingsButton.renderButton(p_drawScreen_1_, p_drawScreen_2_, sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 + 25);
        this.quitButton.renderButton(p_drawScreen_1_, p_drawScreen_2_, sr.getScaledWidth() / 2 - -80, sr.getScaledHeight() / 2 + 25);
        this.singlePlayerButton.renderButton(p_drawScreen_1_, p_drawScreen_2_, sr.getScaledWidth() / 2 - 160, sr.getScaledHeight() / 2 + 25);
        this.multiPlayerButton.renderButton(p_drawScreen_1_, p_drawScreen_2_, sr.getScaledWidth() / 2 - 80, sr.getScaledHeight() / 2 + 25);
        this.alts.renderButton(p_drawScreen_1_, p_drawScreen_2_, 10, 10);
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
    }

    protected void mouseClicked(int p_mouseClicked_1_, int p_mouseClicked_2_, int p_mouseClicked_3_) throws IOException {
        if (this.singlePlayerButton.isPressed(p_mouseClicked_1_, p_mouseClicked_2_)) {
            this.mc.displayGuiScreen((GuiScreen) new GuiWorldSelection((GuiScreen) this));
        } else if (this.multiPlayerButton.isPressed(p_mouseClicked_1_, p_mouseClicked_2_)) {
            this.mc.displayGuiScreen((GuiScreen) new GuiMultiplayer((GuiScreen) this));
        } else if (this.settingsButton.isPressed(p_mouseClicked_1_, p_mouseClicked_2_)) {
            this.mc.displayGuiScreen((GuiScreen) new GuiOptions((GuiScreen) this, this.mc.gameSettings));
        } else if (this.quitButton.isPressed(p_mouseClicked_1_, p_mouseClicked_2_)) {
            this.mc.shutdown();
        } else if (this.alts.isPressed(p_mouseClicked_1_, p_mouseClicked_2_)) {
            //this.mc.displayGuiScreen((GuiScreen) new GuiAltManager(this));
        }
        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_2_, p_mouseClicked_3_);
    }

    public class Button {
        private final String s;
        private final int w;
        private final int h;
        private final Color color;
        private final Color colorPressed;
        private int x;
        private int y;

        public Button(String s, int x, int y, int w) {
            this.s = s;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = 20;
            this.color = new Color(116, 116, 116, 64);
            this.colorPressed = new Color(116, 116, 116, 153);
        }

        public void renderButton(int mouseX, int mouseY, int x, int y) {
            this.x = x;
            this.y = y;
            RenderUtil.roundedRect(x, y, this.w, this.h, 10, this.color);
            RenderUtil.roundedRect(x, y, this.w, this.h, 10, RenderUtil.isHovered(x, y, this.w, this.h, mouseX, mouseY) ? this.colorPressed : this.color);
            mc.fontRenderer.drawString(this.s, x + this.w / 2 - mc.fontRenderer.getStringWidth(this.s) / 2, y + this.h / 2 - 4, -1);

        }

        public boolean isPressed(int mouseX, int mouseY) {
            return RenderUtil.isHovered(this.getX(), this.getY(), this.getW(), this.getH(), mouseX, mouseY);
        }

        public int getW() {
            return this.w;
        }

        public int getH() {
            return this.h;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public String getString() {
            return this.s;
        }
    }
}