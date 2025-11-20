package me.molybdenum.ambience_mini.engine.loader.interpreter.values;

public final class IntVal extends Value {
    public final int value;

    public IntVal(int value) {
        this.value = value;
    }

    public String toString() {
        return Integer.toString(value);
    }
}
