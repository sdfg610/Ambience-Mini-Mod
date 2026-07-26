package me.molybdenum.ambience_mini.engine.client.core.monitor.music_selector.values.helpers;

import me.molybdenum.ambience_mini.engine.client.core.monitor.music_selector.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ValueMap extends HashMap<Value<?>, Value<?>> {
    public ValueMap() { }

    public ValueMap(@NotNull HashMap<Value<?>, Value<?>> map) {
        super(map);
    }
}
