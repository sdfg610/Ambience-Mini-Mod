package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.AccessibleV;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FloatVal extends Value<Float> implements AccessibleV
{
    public static final FloatVal UNDEFINED = new FloatVal();


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

    @Override
    public Value<?> getField(String field) {
        return "intVal".equals(field) ? new IntVal(value.intValue()) : IntVal.UNDEFINED;
    }
}
