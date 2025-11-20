package me.molybdenum.ambience_mini.engine.loader.interpreter.values;

public final class BoolVal extends Value {
    public final boolean value;

    public BoolVal(boolean value) {
        this.value = value;
    }

    public String toString() {
        return Boolean.toString(value);
    }
}
