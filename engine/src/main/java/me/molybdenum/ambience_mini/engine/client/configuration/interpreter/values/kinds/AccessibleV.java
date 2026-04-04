package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.Value;

public interface AccessibleV {
    Value<?> getField(String field);
}
