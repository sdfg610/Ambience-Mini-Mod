package me.molybdenum.ambience_mini.engine.loader.interpreter.values;

public final class FloatVal extends Value {
    public final float value;

    public FloatVal(float value) {
        this.value = value;
    }

    public String toString() {
        return Float.toString(value);
    }
}
