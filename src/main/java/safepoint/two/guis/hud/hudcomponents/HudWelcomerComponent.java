package safepoint.two.guis.hud.hudcomponents;

import safepoint.two.Safepoint;
import safepoint.two.guis.hud.HudModule;
import safepoint.two.module.core.ClickGui;
import safepoint.two.utils.render.RenderUtil;

import java.awt.*;

import static safepoint.two.Safepoint.mc;

public class HudWelcomerComponent extends HudModule {
    int dragX;
    int dragY;
    boolean isDragging;

    public HudWelcomerComponent() {
        super("Welcomer");
        renderX = 500;
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
                RenderUtil.drawRect(renderX, renderY, renderX + mc.fontRenderer.getStringWidth("Welcome to Safepoint + 2, " + mc.player.getName()), renderY + mc.fontRenderer.FONT_HEIGHT, new Color(0, 0, 0, 100).getRGB());
                RenderUtil.drawRect(renderX + mc.fontRenderer.getStringWidth("Welcome to Safepoint + 2, " + mc.player.getName()) + 3, renderY - 7, renderX + mc.fontRenderer.getStringWidth("Welcome to Safepoint + 2, " + mc.player.getName()) + 3 + mc.fontRenderer.getStringWidth("renderX: " + renderX + " renderY: " + renderY), renderY - 7 + mc.fontRenderer.FONT_HEIGHT, new Color(0, 0, 0, 100).getRGB());
                mc.fontRenderer.drawStringWithShadow("renderX: " + renderX + " renderY: " + renderY, renderX + mc.fontRenderer.getStringWidth("Welcome to Safepoint + 2, " + mc.player.getName()) + 3, renderY - 7, -1);
            }
            drawText();
        }
    }

    public void drawText() {
        mc.fontRenderer.drawStringWithShadow("Welcome to Safepoint + 2, " + mc.player.getName(), renderX, renderY, ClickGui.getInstance().color.getColor().getRGB());
    }

    public boolean isInsideDragField(int mouseX, int mouseY) {
        return (mouseX > renderX && mouseX < renderX + mc.fontRenderer.getStringWidth("Welcome to Safepoint + 2, " + mc.player.getName())) && (mouseY > renderY && mouseY < renderY + mc.fontRenderer.FONT_HEIGHT);
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