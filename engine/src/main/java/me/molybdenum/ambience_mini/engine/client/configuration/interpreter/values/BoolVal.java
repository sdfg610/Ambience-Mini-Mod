package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BoolVal extends Value<Boolean>
{
    public static final BoolVal UNDEFINED = new BoolVal();
    public static final BoolVal FALSE = new BoolVal(false);


    public BoolVal() {
        super(null);
    }

    public BoolVal(Boolean value) {
        super(value);
    }


    @Override
    public String toStringInner(@NotNull Boolean value) {
        return value.toString();
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }
}
