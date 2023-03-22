package safepoint.two.utils.render.animation;

import java.util.function.Supplier;

public class BoundedAnimation extends Animation {

    private float minimum;

    private float maximum;

    public BoundedAnimation(float minimum, float maximum, Supplier<Float> length, boolean initialState, Supplier<Easing> easing) {
        super(length, initialState, easing);

        this.minimum = minimum;
        this.maximum = maximum;
    }

    public BoundedAnimation(float minimum, float maximum, float length, boolean initialState, Easing easing) {
        this(minimum, maximum, () -> length, initialState, () -> easing);
    }

    public BoundedAnimation(float minimum, float maximum, Supplier<Float> length, boolean initialState, Easing easing) {
        this(minimum, maximum, length, initialState, () -> easing);
    }

    public BoundedAnimation(float minimum, float maximum, float length, boolean initialState, Supplier<Easing> easing) {
        this(minimum, maximum, () -> length, initialState, easing);
    }

    public double getAnimationValue() {
        return minimum + ((maximum - minimum) * getAnimationFactor());
    }

    public float getMinimum() {
        return minimum;
    }

    public void setMinimum(float minimum) {
        this.minimum = minimum;
    }

    public float getMaximum() {
        return maximum;
    }

    public void setMaximum(float maximum) {
        this.maximum = maximum;
    }
}
