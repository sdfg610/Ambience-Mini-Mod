package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class BoolT extends Type {
    public static final BoolT INSTANCE = new BoolT();

    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof BoolT;
    }
}
