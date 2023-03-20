package safepoint.two.core.event.events;

import safepoint.two.core.event.EventProcessor;

import net.minecraft.client.gui.GuiScreen;

public class DisplayGuiScreenEvent
        extends EventProcessor {
    private GuiScreen screen;

    public DisplayGuiScreenEvent(GuiScreen screen) {
        this.screen = screen;
    }

    public GuiScreen getScreen() {
        return this.screen;
    }

    public void setScreen(GuiScreen screen) {
        this.screen = screen;
    }

    public static class Closed
            extends DisplayGuiScreenEvent {
        public Closed(GuiScreen screen) {
            super(screen);
        }
    }

    public static class Displayed
            extends DisplayGuiScreenEvent {
        public Displayed(GuiScreen screen) {
            super(screen);
        }
    }
}
