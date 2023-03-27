package safepoint.two.guis.hud.hudcomponents;

import safepoint.two.Safepoint;
import safepoint.two.guis.hud.HudModule;
import safepoint.two.module.core.ClickGui;
import safepoint.two.utils.render.RenderUtil;

import java.awt.*;

public class HudWatermarkComponent extends HudModule {
    int dragX;
    int dragY;
    boolean isDragging;

    public HudWatermarkComponent() {
        super("Watermark");
        renderX = 0;
        renderY = 0;
    }

    public void dragScreen(int mouseX, int mouseY) {
        if (!isDragging)
            return;
        renderX = dragX + mouseX;
        renderY = dragY + mouseY;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        dragScreen(mouseX, mouseY);
        if (getValue()) {
            if (isInsideDragField(mouseX, mouseY)) {
                RenderUtil.drawRect(renderX, renderY, renderX + Safepoint.mc.fontRenderer.getStringWidth("Mint 0.1.1"), renderY + Safepoint.mc.fontRenderer.FONT_HEIGHT, new Color(0, 0, 0, 100).getRGB());
                RenderUtil.drawRect(renderX + Safepoint.mc.fontRenderer.getStringWidth("Mint 0.1.1") + 3, renderY - 7, renderX + Safepoint.mc.fontRenderer.getStringWidth("Mint 0.1.1") + 3 + Safepoint.mc.fontRenderer.getStringWidth("renderX: " + renderX + " renderY: " + renderY), renderY - 7 + Safepoint.mc.fontRenderer.FONT_HEIGHT, new Color(0, 0, 0, 100).getRGB());
                Safepoint.fontInitializer.drawString("renderX: " + renderX + " renderY: " + renderY, renderX + Safepoint.mc.fontRenderer.getStringWidth("Mint 0.1.1") + 3, renderY - 7, -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
            }
            drawText();
        }
    }

    public void drawText() {
        Safepoint.fontInitializer.drawString("Safepoint 1.0", renderX, renderY, ClickGui.getInstance().color.getColor().getRGB(),ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
    }

    public boolean isInsideDragField(int mouseX, int mouseY) {
        return (mouseX > renderX && mouseX < renderX + Safepoint.mc.fontRenderer.getStringWidth("Mint 0.1.1")) && (mouseY > renderY && mouseY < renderY + Safepoint.mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isInsideDragField(mouseX, mouseY)) {
            dragX = renderX - mouseX;
            dragY = renderY - mouseY;
            isDragging = true;
        }
        if (mouseButton == 0 && isInside(mouseX, mouseY))
            value = !value;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0)
            isDragging = false;
    }
}
