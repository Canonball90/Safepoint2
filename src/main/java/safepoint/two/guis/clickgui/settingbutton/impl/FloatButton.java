package safepoint.two.guis.clickgui.settingbutton.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.FloatSetting;
import safepoint.two.utils.render.RenderUtil;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FloatButton extends Button {

    int minimax;
    FloatSetting floatSetting;
    Number min;
    Number max;

    public FloatButton(Setting setting, FloatSetting floatSetting) {
        super(setting);
        this.floatSetting = floatSetting;
        min = floatSetting.getMinimum();
        max = floatSetting.getMaximum();
        minimax = max.intValue() - min.intValue();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragSlider(mouseX, mouseY);
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (floatSetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x40000000);
        if (floatSetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x, y + height, floatSetting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        RenderUtil.drawRect(x, y+11, ((Number) floatSetting.getValue()).floatValue() <= floatSetting.getMinimum() ? x : x + ((float) width + 2f) * ((((Number) floatSetting.getValue()).floatValue() - floatSetting.getMinimum()) / (floatSetting.getMaximum() - floatSetting.getMinimum())) - 2, y + (float) height-2, floatSetting.module.category.getColor().getRGB());
        Safepoint.mc.fontRenderer.drawStringWithShadow(floatSetting.getName() + " " + ChatFormatting.GRAY + roundNumber(floatSetting.getValue(), 1), x + 2, y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f), -1);
    }

    void dragSlider(int mouseX, int mouseY) {
        if (isInsideProper(mouseX, mouseY) && Mouse.isButtonDown(0))
            setSliderValue(mouseX);
    }

    public boolean isInsideProper(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + width - 3) && (mouseY > y && mouseY < y + height);
    }

    public static float roundNumber(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal decimal = BigDecimal.valueOf(value);
        decimal = decimal.setScale(places, RoundingMode.FLOOR);
        return decimal.floatValue();
    }


    void setSliderValue(int mouseX) {
        float percent = ((float) mouseX - x - 1) / ((float) width - 5);
        if (floatSetting.getValue() != null) {
            float result = floatSetting.getMinimum() + (float) minimax * percent;
            floatSetting.setValue((float) Math.round(10.0f * result) / 10.0f);
        }
    }

}
