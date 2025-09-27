package me.molybdenum.ambience_mini.engine.state;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.*;

import java.util.function.Supplier;

public class Property {
    public final String name;
    public final Type type;
    private final Supplier<Object> getter;

    public Property(String name, Type type, Supplier<Object> getter) {
        this.name = name;
        this.type = type;
        this.getter = getter;
    }

    public Object getValue() {
        return getter.get();
    }
}
