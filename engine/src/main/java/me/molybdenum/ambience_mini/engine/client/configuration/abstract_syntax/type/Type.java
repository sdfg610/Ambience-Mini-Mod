package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public sealed abstract class Type permits AnyT, AreaT, BoolT, FloatT, IntT, ListT, MapT, PlaylistT, StringT
{
    public boolean equalTo(Type other) {
        return this instanceof AnyT
                || other instanceof AnyT
                || (other != null && equalToInternal(other));
    }

    protected abstract boolean equalToInternal(@NotNull Type other);


    public boolean isBool() {
        return this instanceof BoolT || this instanceof AnyT;
    }

    public boolean isInt() {
        return this instanceof IntT || this instanceof AnyT;
    }

    public boolean isFloat() {
        return this instanceof FloatT || this instanceof AnyT;
    }

    public boolean isString() {
        return this instanceof StringT || this instanceof AnyT;
    }

    public boolean isPlaylist() {
        return this instanceof PlaylistT || this instanceof AnyT;
    }

    public boolean isList() {
        return this instanceof ListT || this instanceof AnyT;
    }
}
