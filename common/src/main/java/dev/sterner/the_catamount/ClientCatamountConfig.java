package dev.sterner.the_catamount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientCatamountConfig {
    private static boolean hudEnabled = false;
    private static final List<String> recentEvents = new ArrayList<>();
    private static final int MAX_EVENTS_DISPLAY = 5;
    private static final int EVENT_DISPLAY_TIME = 20 * 30;
    private static final Map<String, Integer> eventTimers = new HashMap<>();

    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    public static void toggleHud() {
        hudEnabled = !hudEnabled;
    }

    public static void setHudEnabled(boolean enabled) {
        hudEnabled = enabled;
    }

    public static void addEvent(String eventName) {
        if (recentEvents.size() >= MAX_EVENTS_DISPLAY) {
            String oldest = recentEvents.remove(0);
            eventTimers.remove(oldest);
        }
        recentEvents.add(eventName);
        eventTimers.put(eventName, EVENT_DISPLAY_TIME);
    }

    public static List<String> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }

    public static void tick() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : eventTimers.entrySet()) {
            int newTime = entry.getValue() - 1;
            if (newTime <= 0) {
                toRemove.add(entry.getKey());
            } else {
                eventTimers.put(entry.getKey(), newTime);
            }
        }

        for (String event : toRemove) {
            recentEvents.remove(event);
            eventTimers.remove(event);
        }
    }

    public static int getEventTimer(String eventName) {
        return eventTimers.getOrDefault(eventName, 0);
    }

    public static void clear() {
        recentEvents.clear();
        eventTimers.clear();
    }
}