package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FloatVal extends Value<Float>
{
    public static final FloatVal UNDEFINED = new FloatVal();
    public static final FloatVal ZERO = new FloatVal(0f);


    public FloatVal() {
        super(null);
    }

    public FloatVal(Float value) {
        super(value);
    }


    @Override
    public String toStringInner(@NotNull Float value) {
        return value.toString();
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }
}
