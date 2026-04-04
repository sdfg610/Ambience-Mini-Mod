package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class IntVal extends Value<Integer>
{
    public static final IntVal UNDEFINED = new IntVal();
    public static final IntVal ZERO = new IntVal(0);


    public IntVal() {
        super(null);
    }

    public IntVal(int value) {
        super(value);
    }


    @Override
    public String toStringInner(@NotNull Integer value) {
        return value.toString();
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }
}
