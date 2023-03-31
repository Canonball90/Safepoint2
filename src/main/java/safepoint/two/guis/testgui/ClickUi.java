package safepoint.two.guis.testgui;

import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.ChildBasedSizeConstraint;
import gg.essential.elementa.constraints.ConstantColorConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.ScissorEffect;
import safepoint.two.guis.clickgui.windows.Window;

import java.awt.*;
import java.util.ArrayList;

public class ClickUi extends WindowScreen {
    UIComponent box = new UIBlock()
            .setX(new PixelConstraint(10f))
            .setY(new PixelConstraint(10f))
            .setWidth(new PixelConstraint(36f))
            .setHeight(new PixelConstraint(36f))
            .setChildOf(getWindow())
            .enableEffect(new ScissorEffect());

    public ClickUi() {
        super(ElementaVersion.V2);

        box.onMouseEnterRunnable(() -> {
            // Animate, set color, etc.
            AnimatingConstraints anim = box.makeAnimation();
            anim.setColorAnimation(Animations.OUT_EXP, 0.5f, new ConstantColorConstraint(new Color(50,255, 2)));
            //anim.setWidthAnimation(Animations.OUT_EXP, 1.5f, new ChildBasedSizeConstraint(2f));
            anim.onCompleteRunnable(() -> {
                anim.setWidthAnimation(Animations.IN_EXP, 0.5f, new ChildBasedSizeConstraint(2f));
            });
            box.animateTo(anim);
        });
        box.onMouseReleaseRunnable(() -> {
            AnimatingConstraints anim = box.makeAnimation();
            anim.setHeightAnimation(Animations.OUT_EXP, 1f, new ChildBasedSizeConstraint(2f));
        });
    }
}
