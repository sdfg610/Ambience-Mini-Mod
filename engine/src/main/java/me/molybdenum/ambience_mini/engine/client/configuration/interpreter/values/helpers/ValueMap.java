package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ValueMap extends HashMap<Value<?>, Value<?>> {
    public ValueMap() { }

    public ValueMap(@NotNull HashMap<Value<?>, Value<?>> map) {
        super(map);
    }
}
