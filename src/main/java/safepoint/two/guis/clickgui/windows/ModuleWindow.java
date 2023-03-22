package safepoint.two.guis.clickgui.windows;

import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.guis.clickgui.settingbutton.impl.*;
import safepoint.two.core.module.Module;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.*;
import safepoint.two.utils.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import safepoint.two.utils.render.animation.Animation;
import safepoint.two.utils.render.animation.Easing;

import java.awt.*;
import java.util.ArrayList;

public class ModuleWindow {
    public String name;
    public int x;
    public int y;
    public int width;
    public int height;
    public Color disabledColor;
    public Color enabledColor;
    public Module module;
    ArrayList<Button> newButton = new ArrayList<>();
    Animation animation = new Animation(() -> 200f, false, () -> Easing.LINEAR);

    public ModuleWindow(String name, int x, int y, int width, int height, Color disabledColor, Color enabledColor, Module module) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.disabledColor = disabledColor;
        this.enabledColor = enabledColor;
        this.module = module;
        getSettings();
    }

    public void getSettings() {
        ArrayList<Button> settingList = new ArrayList<>();
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting && !setting.getName().equals("Enabled"))
                settingList.add(new BooleanButton(setting));
            if (setting instanceof IntegerSetting)
                settingList.add(new IntegerButton(setting, (IntegerSetting) setting));
            if (setting instanceof FloatSetting)
                settingList.add(new FloatButton(setting, (FloatSetting) setting));
            if (setting instanceof DoubleSetting)
                settingList.add(new DoubleButton(setting, (DoubleSetting) setting));
            if (setting instanceof EnumSetting)
                settingList.add(new EnumButton(setting, (EnumSetting) setting));
            if (setting instanceof StringSetting)
                settingList.add(new StringButton(setting, (StringSetting) setting));
            if (setting instanceof ColorSetting)
                settingList.add(new ColorButton(setting, (ColorSetting) setting));
            if (setting instanceof ParentSetting)
                settingList.add(new ParentButton(setting, (ParentSetting) setting));
            if (setting instanceof KeySetting)
                settingList.add(new KeyButton(setting, (KeySetting) setting));
        }
        newButton = settingList;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x, y, x + width, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        if (module.isEnabled())
            RenderUtil.drawRect(x + 1, y, x + width - 1, y + height, module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x + 1, y, x + width - 1, y + height, new Color(0, 0, 0, 100).getRGB());
        Safepoint.mc.fontRenderer.drawStringWithShadow(name, isInside(mouseX, mouseY) ? x + 3 : x + 2, y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)+1, -1);
        if (module.isOpened) {
            int y = this.y;
            for (Button button : newButton) {
                if (button.isVisible() && !button.getSetting().getName().equals("Enabled")) {
                    button.setX(x + 2);
                    button.setY(y += height);
                    button.setWidth(width - 4);
                    button.setHeight(height);
                    button.drawScreen(mouseX, mouseY, partialTicks);
                    if (button instanceof ColorButton && ((ColorButton) button).getColorSetting().isOpen()) {
                        y += 112;
                        if (((ColorButton) button).getColorSetting().isSelected())
                            y += 10;
                    }
                    if (button instanceof EnumButton)
                        if (((EnumButton) button).enumSetting.droppedDown) {
                            y += ((EnumButton) button).enumSetting.getModes().size() * 15;
                        }
                }
            }
            //RenderUtil.drawOutlineRect(x + 2, this.y + height, x + width - 2, y + height - 1, module.category.getColor(), 1f);
        }
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1 && isInside(mouseX, mouseY)) {
            module.isOpened = !module.isOpened;
            Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            animation.setState(!animation.getState());
        }
        if (isInside(mouseX, mouseY) && mouseButton == 0)
            if (module.isEnabled())
                module.disableModule();
            else module.enableModule();

        if(animation.getAnimationFactor() > 0) {
            newButton.forEach(newButton -> newButton.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }

    public void initGui() {
        if (module.isOpened)
            newButton.forEach(Button::initGui);
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (animation.getAnimationFactor() > 0) {
            newButton.forEach(newButton -> newButton.onKeyTyped(typedChar, keyCode));
        }
    }

    public boolean isInside(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height);
    }

    public int getHeight() {
        return height;
    }
}
