package me.molybdenum.ambience_mini.engine.configuration.interpreter.values;

public final class StringVal extends Value {
    public final String value;

    public StringVal(String value) {
        this.value = value;
    }

    public String toString() {
        return '"' + value.replace("\"", "\\\"") + '"';
    }
}
