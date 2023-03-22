package safepoint.two.utils.render.animation;

import java.awt.*;
import java.util.function.Supplier;

public class ColourAnimation extends Animation {

    public Color from;

    public Color to;

    public ColourAnimation(Color from, Color to, Supplier<Float> length, boolean initialState, Supplier<Easing> easing) {
        super(length, initialState, easing);

        this.from = from;
        this.to = to;
    }

    public ColourAnimation(Color from, Color to, float length, boolean initialState, Easing easing) {
        this(from, to, () -> length, initialState, () -> easing);
    }

    public ColourAnimation(Color from, Color to, Supplier<Float> length, boolean initialState, Easing easing) {
        this(from, to, length, initialState, () -> easing);
    }

    public ColourAnimation(Color from, Color to, float length, boolean initialState, Supplier<Easing> easing) {
        this(from, to, () -> length, initialState, easing);
    }

    public Color getColour() {
        double factor = getAnimationFactor();

        return new Color(
                (int) (from.getRed() + (to.getRed() - from.getRed()) * factor),
                (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * factor),
                (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * factor),
                (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * factor)
        );
    }

}
