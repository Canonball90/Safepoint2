package safepoint.two.guis.clickgui.settingbutton.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.EnumSetting;
import safepoint.two.utils.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;


public class EnumButton extends Button {
    Setting setting;
    public EnumSetting enumSetting;

    public EnumButton(Setting setting, EnumSetting modeSetting) {
        super(setting);
        this.setting = setting;
        this.enumSetting = modeSetting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (setting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x40000000);
        if (setting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x, y + height, setting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        Safepoint.fontInitializer.drawString(enumSetting.getName() + " " + ChatFormatting.GRAY + enumSetting.getValueAsString(), x + 2, (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)+1), -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
        int y = this.y;
        if (enumSetting.droppedDown) {
            for (String string : enumSetting.getModes()) {
                y += 15;
                RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
                RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
                RenderUtil.drawRect(x, y-15, x + 1, y + height, setting.module.category.getColor().getRGB());
                if (mouseX > x+3 && mouseX < x + width-1 && mouseY > y+1 && mouseY < y + height-1)
                    RenderUtil.drawRect(x+ 3, y+1, x + width - 1, y + height-1, new Color(0, 0, 0, 100).getRGB());
                Safepoint.fontInitializer.drawString(enumSetting.getValue().equals(string) ? string : ChatFormatting.GRAY + string, (mouseX > x+3 && mouseX < x + width-1 && mouseY > y+1 && mouseY < y + height-1) ? x + 5 : x + 4, y+4, -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
            }
            RenderUtil.drawOutlineRect(x + 3, this.y + height+1, x + width - 1, y + height-1, setting.module.category.getColor(), 1f);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isInside(mouseX, mouseY) && mouseButton == 1) {
            enumSetting.droppedDown = !enumSetting.droppedDown;
        }
        int y = this.y;
        if (enumSetting.droppedDown)
            for (String string : enumSetting.getModes()) {
                y += 15;
                if (mouseX > x+3 && mouseX < x + width-1 && mouseY > y+1 && mouseY < y + height-1 && mouseButton == 0) {
                    enumSetting.setValue(string);
                }

            }

        Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
}
