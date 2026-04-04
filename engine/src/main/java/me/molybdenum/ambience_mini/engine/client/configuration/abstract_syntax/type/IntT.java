package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class IntT extends Type {
    public static final IntT INSTANCE = new IntT();

    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof IntT;
    }
}
