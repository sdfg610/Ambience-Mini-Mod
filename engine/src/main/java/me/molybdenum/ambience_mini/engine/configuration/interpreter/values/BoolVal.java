package me.molybdenum.ambience_mini.engine.configuration.interpreter.values;

public final class BoolVal extends Value
{
    public static final BoolVal FALSE = new BoolVal(false);

    public final boolean value;

    public BoolVal(boolean value) {
        this.value = value;
    }

    public String toString() {
        return Boolean.toString(value);
    }
}
