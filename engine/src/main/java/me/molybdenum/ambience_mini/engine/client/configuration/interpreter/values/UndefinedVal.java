package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import org.jetbrains.annotations.NotNull;

public final class UndefinedVal extends Value<Object> {
    public static final UndefinedVal INSTANCE = new UndefinedVal();


    public UndefinedVal() {
        super(null);
    }

    @Override
    public String toStringInner(@NotNull Object value) {
        return "undefined";
    }

    @Override
    public boolean equals(Value<?> other) {
        return other.value == null;
    }
}
