package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.Type;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.BoolVal;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.utils.Pair;
import me.molybdenum.ambience_mini.engine.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;


public abstract class BaseGameStateProvider {
    private final ArrayList<Event> _events = new ArrayList<>();
    private final ArrayList<Property> _properties = new ArrayList<>();

    private final ArrayList<BiConsumer<String, Value>> _onFiredListeners = new ArrayList<>();


    // -----------------------------------------------------------------------------------------------------------------
    // Registration
    @SuppressWarnings("UnusedReturnValue")
    protected Event registerEvent(String name, Supplier<BoolVal> isActive) {
        if (_events.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-event: " + name);

        Event ev = new Event(name, isActive, this::onEventFired);
        _events.add(ev);
        return ev;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected Property registerProperty(String name, Type type, Supplier<Value> getter) {
        if (_properties.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-property: " + name);

        Property pr = new Property(name, type, getter, this::onPropertyFired);
        _properties.add(pr);
        return pr;
    }


    public void registerOnFiredListener(BiConsumer<String, Value> onFired) {
        _onFiredListeners.add(onFired);
    }

    public void unregisterOnFiredListener(BiConsumer<String, Value> onFired) {
        _onFiredListeners.remove(onFired);
    }


    private void onEventFired(Event event, BoolVal boolVal) {
        for (var listener : _onFiredListeners)
            listener.accept('@' + event.name, boolVal);
    }

    private void onPropertyFired(Property property, Value value) {
        for (var listener : _onFiredListeners)
            listener.accept('$' + property.name, value);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Lookup
    public Event getEvent(String name) {
        return _events.stream()
                .filter(event -> event.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such event: " + name));
    }

    public Property getProperty(String name) {
        return _properties.stream()
                .filter(property -> property.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such property: " + name));
    }


    public Optional<Event> tryGetEvent(String name) {
        return _events.stream()
                .filter(event -> event.name.equals(name))
                .findFirst();
    }

    public Optional<Property> tryGetProperty(String name) {
        return _properties.stream()
                .filter(property -> property.name.equals(name))
                .findFirst();
    }


    public List<Event> getEvents() {
        return _events;
    }

    public List<Property> getProperties() {
        return _properties;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Execution
    public abstract void prepare(@Nullable ArrayList<String> messages);


    // -----------------------------------------------------------------------------------------------------------------
    // Debugging
    public String readAll() {
        ArrayList<Pair<String, Value>> readings = new ArrayList<>();

        for (var event : _events)
            readings.add(new Pair<>("@" + event.name, event.isActive()));
        for (var property : _properties)
            readings.add(new Pair<>("$" + property.name, property.getValue()));

        return Utils.getKeyValuePairString(readings);
    }
}
