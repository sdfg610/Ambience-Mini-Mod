package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class FloatT extends Type {
    public static final FloatT INSTANCE = new FloatT();

    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof FloatT;
    }
}
