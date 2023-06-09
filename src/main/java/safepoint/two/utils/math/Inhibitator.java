package safepoint.two.utils.math;

import safepoint.two.core.settings.impl.DoubleSetting;

public class Inhibitator {

    public  boolean shuldMoveRight = true;
    public  boolean shuldMoveLeft = false;

    public Timer timer = new Timer();

    public void doInhibitation(DoubleSetting settingToInhibit, double inhibitationSpeed, double reachToFirst, double reachToSecond) {

        if (settingToInhibit.getValue() >= reachToFirst) {
            returner();
        }

        if (settingToInhibit.getValue() <= reachToSecond) {
            returner2();
        }

        if (shuldMoveRight) {
            if (timer.passedMs((long) inhibitationSpeed)) {
                settingToInhibit.setValue((double) Math.round(settingToInhibit.getValue()+1));
                timer.reset();
            }
        }

        if (shuldMoveLeft) {
            if (timer.passedMs((long) inhibitationSpeed)) {
                settingToInhibit.setValue((double) Math.round(settingToInhibit.getValue() - 1));
                timer.reset();
            }
        }

    }

    public void doInhibitation(DoubleSetting settingToInhibit, double inhibitationSpeed, double reachToFirst, double reachToSecond, int add) {

        if (settingToInhibit.getValue() >= reachToFirst) {
            returner();
        }

        if (settingToInhibit.getValue() <= reachToSecond) {
            returner2();
        }

        if (shuldMoveRight) {
            if (timer.passedMs((long) inhibitationSpeed)) {
                settingToInhibit.setValue((double) Math.round(settingToInhibit.getValue()+add));
                timer.reset();
            }
        }

        if (shuldMoveLeft) {
            if (timer.passedMs((long) inhibitationSpeed)) {
                settingToInhibit.setValue((double) Math.round(settingToInhibit.getValue() - add));
                timer.reset();
            }
        }

    }


    public void returner() {
        shuldMoveRight = false;
        shuldMoveLeft = true;
    }

    public void returner2() {
        shuldMoveRight = true;
        shuldMoveLeft = false;
    }
}
