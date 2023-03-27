package safepoint.two.core.initializers;

import safepoint.two.Safepoint;

import static safepoint.two.Safepoint.mc;

public class FontInitializer {

    public void drawString(String text, int x, int y, int Color, Boolean shadow, Boolean custom){
        if(custom){
            Safepoint.OVERLAY_FONT.drawString(text,x,y,Color,shadow);
        }else{
            mc.fontRenderer.drawString(text,x,y,Color,shadow);
        }
    }

    public int getStringWidth(String text, Boolean customFont){
        if(customFont){
            return Safepoint.OVERLAY_FONT.getStringWidth(text);
        }else{
            return mc.fontRenderer.getStringWidth(text);
        }
    }

    public void drawCenteredString(String text, int x, int y, int color, Boolean shadow){
        if(shadow){
            Safepoint.OVERLAY_FONT.drawCenteredStringWithShadow(text, x, y, color);
        }else {
            Safepoint.OVERLAY_FONT.drawCenteredString(text, x, y, color);
        }
    }
}
