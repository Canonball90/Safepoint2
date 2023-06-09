package safepoint.two.guis.clickgui.settingbutton.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.KeySetting;
import safepoint.two.utils.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class KeyButton extends Button {
    KeySetting keySetting;

    public KeyButton(Setting setting, KeySetting keySetting) {
        super(setting);
        this.keySetting = keySetting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (keySetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x40000000);
        if (keySetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x, y + height, keySetting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        Safepoint.fontInitializer.drawString(keySetting.isTyping ? keySetting.getName() + " ..." : keySetting.getName() + " " + ChatFormatting.GRAY + (keySetting.getKey() == -1 ? "None" : Keyboard.getKeyName(keySetting.getKey())), x + 2, (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)), -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isInside(mouseX, mouseY)) {
            keySetting.isTyping = !keySetting.isTyping;
            Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (!keySetting.isTyping)
            return;
        if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_ESCAPE)
            keySetting.setBind(0);
        else keySetting.setBind(keyCode);

        Safepoint.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        keySetting.isTyping = !keySetting.isTyping;
    }
}
