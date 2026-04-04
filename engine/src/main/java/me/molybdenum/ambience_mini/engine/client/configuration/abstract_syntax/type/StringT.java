package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class StringT extends Type {
    public static final StringT INSTANCE = new StringT();

    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof StringT;
    }
}
