package safepoint.two.guis.hud;

import safepoint.two.Safepoint;
import safepoint.two.module.core.ClickGui;
import safepoint.two.utils.render.RenderUtil;

public class HudModule {
    int x;
    int y;
    int w;
    int h;
    public int renderX;
    public int renderY;
    String name;
    public boolean value;

    public HudModule(String name) {
        this.name = name;
        this.value = false;
    }

    public void drawScreen() {
        RenderUtil.drawRect(x, y, x + w, y + h,value ? ClickGui.getInstance().color.getColor().getRGB() : ClickGui.getInstance().backgroundColor.getColor().getRGB());
        Safepoint.fontInitializer.drawString(name, x, y, -1,ClickGui.getInstance().shadow.getValue(),ClickGui.getInstance().customFont.getValue());
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
    }

    public void drawText(){
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0 && isInside(mouseX, mouseY))
            value = !value;
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton){
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.w = width;
    }

    public void setHeight(int height) {
        this.h = height;
    }

    public boolean isInside(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + w) && (mouseY > y && mouseY < y + h);
    }
    public String getName(){
        return name;
    }

    public boolean getValue(){
        return value;
    }

    public int getRenderX() {
        return renderX;
    }

    public int getRenderY(){
        return renderY;
    }

    public void setRenderX(int renderX) {
        this.renderX = renderX;
    }

    public void setRenderY(int renderY) {
        this.renderY = renderY;
    }
}
