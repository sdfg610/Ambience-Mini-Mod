package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.kinds.AccessibleT;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class IntT extends Type implements AccessibleT {
    public static final IntT INSTANCE = new IntT();

    public static final Map<String, Type> FIELDS = Map.of(
            "floatVal", FloatT.INSTANCE
    );


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof IntT;
    }

    @Override
    public Map<String, Type> fieldTypes() {
        return FIELDS;
    }
}
