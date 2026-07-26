package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class AnyT extends Type {
    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return true;
    }
}
