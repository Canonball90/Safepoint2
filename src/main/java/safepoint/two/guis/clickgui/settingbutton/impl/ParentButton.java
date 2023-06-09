package safepoint.two.guis.clickgui.settingbutton.impl;

import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.ParentSetting;
import safepoint.two.utils.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

public class ParentButton extends Button {
    Setting setting;
    ParentSetting parentSetting;

    public ParentButton(Setting setting, ParentSetting parentSetting) {
        super(setting);
        this.setting = setting;
        this.parentSetting = parentSetting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (parentSetting.isOpen) RenderUtil.drawRect(x - 2, y, x, y + height, setting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        RenderUtil.drawRect(x+2, y, x + width-2, y + height, setting.module.category.getColor().getRGB());
        Safepoint.fontInitializer.drawString(parentSetting.isOpen ? "-" : "+", x + width - Safepoint.mc.fontRenderer.getStringWidth(parentSetting.isOpen ? "-" : "+") - 4, (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)), -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
        Safepoint.fontInitializer.drawString(parentSetting.getName(), x + 4, (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)), -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1 && isInside(mouseX, mouseY)) {
            parentSetting.isOpen = !parentSetting.isOpen;
            Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }
}
