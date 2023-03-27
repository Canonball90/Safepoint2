package safepoint.two.core.event.events;

import safepoint.two.core.event.EventProcessor;
import safepoint.two.utils.core.SafeRunnable;

import java.util.ArrayDeque;
import java.util.Deque;

public class EventPostSync extends EventProcessor {
    private final Deque<Runnable> postEvents = new ArrayDeque<>();

    public EventPostSync() {

    }

    public void addPostEvent(SafeRunnable runnable) {
        postEvents.add(runnable);
    }


    public Deque<Runnable> getPostEvents() {
        return postEvents;
    }
}
