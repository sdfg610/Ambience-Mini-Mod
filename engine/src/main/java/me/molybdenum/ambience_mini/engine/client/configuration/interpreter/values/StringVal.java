package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class StringVal extends Value<String>
{
    public static final StringVal UNDEFINED = new StringVal();
    public static final StringVal EMPTY = new StringVal("");


    public StringVal() {
        super(null);
    }

    public StringVal(String value) {
        super(value);
    }


    @Override
    public String toStringInner(@NotNull String value) {
        return '"' + value.replace("\"", "\\\"") + '"';
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }
}
