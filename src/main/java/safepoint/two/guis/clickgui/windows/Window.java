package safepoint.two.guis.clickgui.windows;

import safepoint.two.Safepoint;
import safepoint.two.core.module.Module;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.ColorSetting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.utils.math.Timer;
import safepoint.two.utils.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;

public class Window {

    String name;
    int x;
    int y;
    int width;
    int height;
    Module.Category category;

    boolean isDragging;
    int dragX;
    int dragY;
    boolean isOpened;

    ArrayList<ModuleWindow> modules = new ArrayList<>();

    Timer panelTimer = new Timer();

    public Window(String name, int x, int y, int width, int height, Module.Category category) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        isOpened = true;
    }

    public void dragScreen(int mouseX, int mouseY) {
        if (!isDragging)
            return;
        x = dragX + mouseX;
        y = dragY + mouseY;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragScreen(mouseX, mouseY);
        RenderUtil.drawRect(x, y, x + width, y + height, category.getColor().getRGB());
        RenderUtil.drawOutlineRect(x, y, x + width, y + height, category.getColor(), 1.5f);
        Safepoint.fontInitializer.drawString(name, (int) (x + (width / 2f) - (Safepoint.mc.fontRenderer.getStringWidth(name) / 2f)), (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)), -1, ClickGui.getInstance().shadow.getValue(), ClickGui.getInstance().customFont.getValue());
        if (isOpened) {
            modules.clear();
            int y = this.y;
            assert Safepoint.moduleInitializer != null;
            for (Module module : Safepoint.moduleInitializer.getCategoryModules(category)) {
                int openedHeight = 0;
                int index = 0;
                if (module.isOpened) {
                    index++;
                    if (isOpened) {
                        if (!panelTimer.passedMs(index * 25)) continue;
                    } else {
                        if (panelTimer.passedMs((modules.size() - index) * 25)) continue;
                    }
                    assert Safepoint.settingInitializer != null;
                    for (Setting settingsRewrite : module.getSettings()) {
                        if (settingsRewrite.getName().equals("Enabled"))
                            continue;
                        if (settingsRewrite.isVisible())
                            openedHeight += 15;

                        if (settingsRewrite instanceof ColorSetting && settingsRewrite.isVisible()) {
                            if (((ColorSetting) settingsRewrite).isOpen()) {
                                openedHeight += 112;
                                if (((ColorSetting) settingsRewrite).isSelected())
                                    openedHeight += 15;
                            }
                        }
                        if (settingsRewrite instanceof EnumSetting)
                            if (((EnumSetting) settingsRewrite).droppedDown)
                                openedHeight += ((EnumSetting) settingsRewrite).getModes().size() * 15;
                    }
                }
                modules.add(new ModuleWindow(module.getName(), x, y += height, width, height, ClickGui.getInstance().backgroundColor.getColor(), category.getColor(), module));
                y += openedHeight;
            }
            RenderUtil.drawOutlineRect(x, this.y + height, x + width, y + height, category.getColor(), 1.5f);
        }
        RenderUtil.drawOutlineRect(x, this.y, x + width, this.y + height, category.getColor(), 1.3f);
        if (isOpened)
            modules.forEach(modules -> modules.drawScreen(mouseX, mouseY, partialTicks));
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isInside(mouseX, mouseY)) {
            dragX = x - mouseX;
            dragY = y - mouseY;
            isDragging = true;
        }
        if (mouseButton == 1 && isInside(mouseX, mouseY)) {
            isOpened = !isOpened;
            Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            panelTimer.reset();
        }
        if (isOpened)
            modules.forEach(modules -> modules.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (isOpened)
            modules.forEach(modules -> modules.onKeyTyped(typedChar, keyCode));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0)
            isDragging = false;
    }

    public void initGui() {
        if (isOpened)
            modules.forEach(ModuleWindow::initGui);
    }

    public boolean isInside(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height);
    }
}
