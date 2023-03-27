package safepoint.two.guis.clickgui;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.windows.Window;
import safepoint.two.core.module.Module;
import net.minecraft.client.gui.GuiScreen;
import safepoint.two.utils.render.RenderUtil;
import safepoint.two.utils.render.animation.Animation;
import safepoint.two.utils.render.animation.Easing;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ClickGui extends GuiScreen {
    static ClickGui INSTANCE = new ClickGui();
    ArrayList<Window> windows = new ArrayList<>();

    public ClickGui() {
        setInstance();
        load();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(Minecraft.getMinecraft().currentScreen.width - 960, Minecraft.getMinecraft().currentScreen.height - 60, Minecraft.getMinecraft().currentScreen.width - 700, Minecraft.getMinecraft().currentScreen.height + 40, new Color(25,25,25, 100).getRGB());
        drawHead(Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.player.getUniqueID()).getLocationSkin(), Minecraft.getMinecraft().currentScreen.width - 950, Minecraft.getMinecraft().currentScreen.height - 50);
        Safepoint.fontInitializer.drawString("Kills: " + getPlayerKills(), Minecraft.getMinecraft().currentScreen.width - 900, Minecraft.getMinecraft().currentScreen.height - 50, new Color(255, 255, 255, 255).getRGB(), safepoint.two.module.core.ClickGui.getInstance().shadow.getValue(), safepoint.two.module.core.ClickGui.getInstance().customFont.getValue());
        Safepoint.fontInitializer.drawString("Deaths: " + getPlayerDeaths(), Minecraft.getMinecraft().currentScreen.width - 900, Minecraft.getMinecraft().currentScreen.height - 40, new Color(255, 255, 255, 255).getRGB(), safepoint.two.module.core.ClickGui.getInstance().shadow.getValue(), safepoint.two.module.core.ClickGui.getInstance().customFont.getValue());
        Safepoint.fontInitializer.drawString("Time: " + onlineTime(), Minecraft.getMinecraft().currentScreen.width - 900, Minecraft.getMinecraft().currentScreen.height - 30, new Color(255, 255, 255, 255).getRGB(), safepoint.two.module.core.ClickGui.getInstance().shadow.getValue(), safepoint.two.module.core.ClickGui.getInstance().customFont.getValue());

        windows.forEach(windows -> windows.drawScreen(mouseX, mouseY, partialTicks));
    }

    public void drawHead(ResourceLocation skin, int width, int height) {
        GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(skin);
        Gui.drawScaledCustomSizeModalRect(width, height, 8, 8, 8, 8, 37, 37, 64, 64);
    }

    //Get Player kills
    public static int getPlayerKills() {
        return Minecraft.getMinecraft().player.getStatFileWriter().readStat(net.minecraft.stats.StatList.PLAYER_KILLS);
    }

    //Get Player Deaths
    public static int getPlayerDeaths() {
        return Minecraft.getMinecraft().player.getStatFileWriter().readStat(net.minecraft.stats.StatList.DEATHS);
    }

    public static String onlineTime(){
        int timeSeconds = (int) (Safepoint.time / 20);
        int timeMinutes = timeSeconds / 60;
        int timeHours = timeMinutes / 60;
        timeSeconds -= timeMinutes * 60;
        return ChatFormatting.GRAY.toString() + timeHours + ChatFormatting.RESET + " Hours, " + ChatFormatting.GRAY + timeMinutes + ChatFormatting.RESET + " Minutes, " + ChatFormatting.GRAY + timeSeconds + ChatFormatting.RESET + " Seconds.";
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        windows.forEach(windows -> windows.mouseClicked(mouseX, mouseY, clickedButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releasedButton) {
        windows.forEach(windows -> windows.mouseReleased(mouseX, mouseY, releasedButton));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        windows.forEach(windows -> windows.onKeyTyped(typedChar, keyCode));
    }

    @Override
    public void initGui() {
        super.initGui();
        windows.forEach(Window::initGui);
    }

    public void load() {
        int x = -130;
        assert Safepoint.moduleInitializer != null;
        for (Module.Category categories : Safepoint.moduleInitializer.getCategories())
            windows.add(new Window(categories.toString(), x += 134, 15, 130, 15, categories));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
