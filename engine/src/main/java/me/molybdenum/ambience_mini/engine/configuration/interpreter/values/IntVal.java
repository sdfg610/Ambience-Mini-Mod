package me.molybdenum.ambience_mini.engine.configuration.interpreter.values;

public final class IntVal extends Value
{
    public static final IntVal ZERO = new IntVal(0);

    public final int value;

    public IntVal(int value) {
        this.value = value;
    }

    public String toString() {
        return Integer.toString(value);
    }
}
