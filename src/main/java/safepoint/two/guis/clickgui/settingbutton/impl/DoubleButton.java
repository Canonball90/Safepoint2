package safepoint.two.guis.clickgui.settingbutton.impl;


import com.mojang.realmsclient.gui.ChatFormatting;
import safepoint.two.Safepoint;
import safepoint.two.guis.clickgui.settingbutton.Button;
import safepoint.two.module.core.ClickGui;
import safepoint.two.core.settings.Setting;
import safepoint.two.core.settings.impl.DoubleSetting;
import safepoint.two.utils.render.RenderUtil;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DoubleButton extends Button {

    int minimax;
    DoubleSetting doubleSetting;
    Number min;
    Number max;

    public DoubleButton(Setting setting, DoubleSetting doubleSetting) {
        super(setting);
        this.doubleSetting = doubleSetting;
        min = doubleSetting.getMinimum();
        max = doubleSetting.getMaximum();
        minimax = max.intValue() - min.intValue();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragSlider(mouseX, mouseY);
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, ClickGui.getInstance().backgroundColor.getColor().getRGB());
        RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x50000000);
        if (doubleSetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x + width + 2, y + height, 0x40000000);
        if (doubleSetting.hasParentSetting) RenderUtil.drawRect(x - 2, y, x, y + height, doubleSetting.module.category.getColor().getRGB());
        if (isInside(mouseX, mouseY))
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        RenderUtil.drawRect(x, y+11, ((Number) doubleSetting.getValue()).doubleValue() <= doubleSetting.getMinimum() ? x : (float) (x + ((float) width + 2f) * ((((Number) doubleSetting.getValue()).doubleValue() - doubleSetting.getMinimum()) / (doubleSetting.getMaximum() - doubleSetting.getMinimum())) - 2), y + (float) height-2, doubleSetting.module.category.getColor().getRGB());
        Safepoint.fontInitializer.drawString(doubleSetting.getName() + " " + ChatFormatting.GRAY + roundNumber(doubleSetting.getValue(), 2), x + 2, (int) (y + (height / 2f) - (Safepoint.mc.fontRenderer.FONT_HEIGHT / 2f)), -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
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
        if (doubleSetting.getValue() != null) {
            double result = doubleSetting.getMinimum() + (double) ((float) minimax * percent);
            doubleSetting.setValue(roundNumber(10.0 * result, 2) / 10.0);
        }
    }

}
