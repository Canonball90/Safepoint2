package safepoint.two.utils.core;

@FunctionalInterface
public interface SafeRunnable extends Runnable {
    void runSafely() throws Throwable;

    @Override
    default void run() {
        try {
            runSafely();
        } catch (Throwable t) {
            handle(t);
        }
    }

    default void handle(Throwable t) {
        t.printStackTrace();
    }

}

