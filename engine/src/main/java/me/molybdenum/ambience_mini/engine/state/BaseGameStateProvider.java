package me.molybdenum.ambience_mini.engine.state;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.Type;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class BaseGameStateProvider {
    private static final ArrayList<Event> EVENTS = new ArrayList<>();
    private static final ArrayList<Property> PROPERTIES = new ArrayList<>();


    public void registerEvent(String name, Supplier<Boolean> isActive) {
        if (EVENTS.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-event: " + name);

        EVENTS.add(new Event(name, isActive));
    }

    public void registerProperty(String name, Type type, Supplier<Object> getter) {
        if (PROPERTIES.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-property: " + name);

        PROPERTIES.add(new Property(name, type, getter));
    }


    public Event getEvent(String name) {
        return EVENTS.stream()
                .filter(event -> event.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such event: " + name));
    }

    public Property getProperty(String name) {
        return PROPERTIES.stream()
                .filter(property -> property.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such property: " + name));
    }


    public Optional<Event> tryGetEvent(String name) {
        return EVENTS.stream()
                .filter(event -> event.name.equals(name))
                .findFirst();
    }

    public Optional<Property> tryGetProperty(String name) {
        return PROPERTIES.stream()
                .filter(property -> property.name.equals(name))
                .findFirst();
    }
}
