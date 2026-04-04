package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

public final class MapT extends Type implements IndexableT {
    public final Type keyType;
    public final Type valueType;


    public MapT(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof MapT map
                && keyType != null && valueType != null
                && keyType.equalTo(map.keyType) && valueType.equalTo(map.valueType);
    }

    @Override
    public Type indexerType() {
        return keyType;
    }

    @Override
    public Type outputType() {
        return valueType;
    }
}
