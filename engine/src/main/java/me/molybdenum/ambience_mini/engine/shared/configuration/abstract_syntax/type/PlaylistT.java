package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class PlaylistT extends Type {
    public static final PlaylistT INSTANCE = new PlaylistT();


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof PlaylistT;
    }
}
