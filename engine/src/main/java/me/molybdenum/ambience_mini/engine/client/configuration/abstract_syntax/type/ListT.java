package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.kinds.IndexableT;
import org.jetbrains.annotations.NotNull;

public final class ListT extends Type implements IndexableT {
    public final Type elementType;


    public ListT(Type elementType) {
        this.elementType = elementType;
    }


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof ListT lTyp
                && elementType != null
                && elementType.equalTo(lTyp.elementType);
    }

    @Override
    public Type indexerType() {
        return IntT.INSTANCE;
    }

    @Override
    public Type outputType() {
        return elementType;
    }
}
