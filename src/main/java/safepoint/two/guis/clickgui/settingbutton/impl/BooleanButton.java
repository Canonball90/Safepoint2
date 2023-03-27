package safepoint.two.guis.clickgui.settingbutton.impl;

import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.utils.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

public class BooleanButton extends Button {
    Setting setting;

    public BooleanButton(Setting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (setting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x40000000);
        if (setting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x, y + height, setting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        if ((boolean) setting.getValue())
            RenderUtil.drawRect(x+2, y+3, x + 11, y + height-3, setting.module.category.getColor().getRGB());
        RenderUtil.drawOutlineRect(x+2, y+3, x + 11, y + height-3, new Color(0,0,0), 1.0f);

        Safepoint.fontInitializer.drawString(setting.getName(), x + 15, (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)+1), -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isInside(mouseX, mouseY)) {
            if (getValue())
                setting.setValue(false);
            else setting.setValue(true);
            Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }

    }

    public boolean getValue() {
        return (boolean) setting.getValue();
    }

}
