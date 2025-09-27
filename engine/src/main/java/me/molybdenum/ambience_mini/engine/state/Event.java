package me.molybdenum.ambience_mini.engine.state;

import java.util.function.Supplier;

public class Event {
    public final String name;
    private final Supplier<Boolean> isActive;

    public Event(String name, Supplier<Boolean> isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive.get();
    }
}
