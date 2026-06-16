package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.kinds.AccessibleT;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class FloatT extends Type implements AccessibleT {
    public static final FloatT INSTANCE = new FloatT();

    public static final Map<String, Type> FIELDS = Map.of(
            "intVal", FloatT.INSTANCE
    );


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof FloatT;
    }

    @Override
    public Map<String, Type> fieldTypes() {
        return FIELDS;
    }
}
