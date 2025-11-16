package me.molybdenum.ambience_mini.engine.state.providers;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.Type;
import me.molybdenum.ambience_mini.engine.utils.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BaseGameStateProvider {
    private final ArrayList<Event> _events = new ArrayList<>();
    private final ArrayList<Property> _properties = new ArrayList<>();


    public void registerEvent(String name, Supplier<Boolean> isActive) {
        if (_events.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-event: " + name);

        _events.add(new Event(name, isActive));
    }

    public void registerProperty(String name, Type type, Supplier<Object> getter) {
        if (_properties.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-property: " + name);

        _properties.add(new Property(name, type, getter));
    }


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


    public String readAll() {
        ArrayList<Pair<String, Object>> readings = new ArrayList<>();

        for (var event : _events)
            readings.add(new Pair<>("@" + event.name, event.isActive()));
        for (var property : _properties)
            readings.add(new Pair<>("$" + property.name, property.getValue()));

        int maxKeyLength = readings.stream()
                .map(pair -> pair.left().length())
                .max(Integer::compareTo)
                .orElse(0);

        StringBuilder sb = new StringBuilder();
        for (var reading : readings) {
            sb.append(' ');
            sb.append(padToLength(reading.left(), maxKeyLength));
            sb.append(" = ");
            sb.append(valueToString(reading.right()));
            sb.append('\n');
        }

        return sb.toString();
    }

    private static String padToLength(String str, int length) {
        return String.format("%-" + length + "s", str);
    }

    private static String valueToString(Object value) {
        if (value instanceof List<?> lst) {
            String listString = String.join(
                    ", ",
                    lst.stream().map(BaseGameStateProvider::valueToString).toList()
            );
            return "[ " + listString + " ]";
        }
        else if (value instanceof String str)
            return '"' + str + '"';
        return value.toString();
    }
}
