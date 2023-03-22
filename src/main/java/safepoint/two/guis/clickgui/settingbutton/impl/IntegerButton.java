package safepoint.two.guis.clickgui.settingbutton.impl;


import com.mojang.realmsclient.gui.ChatFormatting;
import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.IntegerSetting;
import safepoint.two.utils.render.RenderUtil;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class IntegerButton extends Button {

    int minimax;
    IntegerSetting integerSetting;

    public IntegerButton(Setting setting, IntegerSetting integerSetting) {
        super(setting);
        this.integerSetting = integerSetting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragSlider(mouseX, mouseY);
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (integerSetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x40000000);
        if (integerSetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x, y + height, integerSetting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        RenderUtil.drawRect(x, y+11, ((Number) integerSetting.getValue()).floatValue() <= integerSetting.getMinimum() ? x : x + ((float) width + 2f) * ((((Number) integerSetting.getValue()).floatValue() - integerSetting.getMinimum()) / (integerSetting.getMaximum() - integerSetting.getMinimum())) - 2, y + (float) height-2,integerSetting.module.category.getColor().getRGB());
        //RenderUtil.drawOutlineRect(x, y+11, ((Number) integerSetting.getValue()).floatValue() <= integerSetting.getMinimum() ? x : x + ((float) width + 2f) * ((((Number) integerSetting.getValue()).floatValue() - integerSetting.getMinimum()) / (integerSetting.getMaximum() - integerSetting.getMinimum())) - 2, y + (float) height-2,new Color(0,0,0), 1.0f);
        Safepoint.mc.fontRenderer.drawStringWithShadow(integerSetting.getName() + " " + ChatFormatting.GRAY + integerSetting.getValue(), x + 2, y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f), -1);
    }

    void dragSlider(int mouseX, int mouseY) {
        if (isInsideExtended(mouseX, mouseY) && Mouse.isButtonDown(0))
            setSliderValue(mouseX);
    }

    public boolean isInsideExtended(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + width + 5) && (mouseY > y && mouseY < y + height);
    }

    void setSliderValue(int mouseX) {
        float percent = ((float) mouseX - x - 1) / ((float) width - 5);
        integerSetting.setValue((int) (integerSetting.getMinimum() + minimax * percent));

        float diff = Math.min(width, Math.max(0, mouseX - x));
        float min = integerSetting.getMinimum();
        float max = integerSetting.getMaximum();
        if (diff == 0) {
            integerSetting.setValue(integerSetting.getMinimum());
        } else {
            float value = roundNumber(diff / width * (max - min) + min, 1);
            integerSetting.setValue((int) value);
        }
    }

    public static float roundNumber(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal decimal = BigDecimal.valueOf(value);
        decimal = decimal.setScale(places, RoundingMode.FLOOR);
        return decimal.floatValue();
    }

}
