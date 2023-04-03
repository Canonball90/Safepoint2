package safepoint.two.core;

import safepoint.two.utils.Global;

public class Core implements Global {
    private final String name;

    public Core(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
