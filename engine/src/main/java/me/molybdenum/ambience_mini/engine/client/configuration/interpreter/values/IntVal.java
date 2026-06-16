package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.AccessibleV;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class IntVal extends Value<Integer> implements AccessibleV
{
    public static final IntVal UNDEFINED = new IntVal();


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

    @Override
    public Value<?> getField(String field) {
        return "floatVal".equals(field) ? new FloatVal(value.floatValue()) : FloatVal.UNDEFINED;
    }
}
