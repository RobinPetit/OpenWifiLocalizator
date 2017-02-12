package be.ulb.owl.event;

import java.util.Comparator;

/**
 * Add a priority to the event
 *
 * @author Detobel36
 */

public enum EventPriority implements Comparator<EventPriority> {
    FIRST(0),
    LAST(1);

    private int _priority;

    EventPriority(int priority) {
        _priority = priority;
    }

    private int getPriority() {
        return _priority;
    }

    // Not used :/
    public int compare(EventPriority ep1, EventPriority ep2) {
        return ep1.getPriority() - ep2.getPriority();
    }

}
