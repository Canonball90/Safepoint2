package safepoint.two.core.decentralized.forge;

import java.awt.*;

public class ForgeInteract {

    private Color cycleColorRGB = Color.WHITE;

    public Color getCycleColorRGB() {
        return cycleColorRGB;
    }

    public void setCycleColorRGB(int hex) {
        this.cycleColorRGB = new Color((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, 255);
    }

}
