package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class AreaT extends Type implements AccessibleT
{
    public static final AreaT INSTANCE = new AreaT();

    public static final Map<String, Type> FIELDS = Map.of(
            "name", StringT.INSTANCE,
            "dimension", StringT.INSTANCE,
            "owner", StringT.INSTANCE,
            "isShared", BoolT.INSTANCE,
            "isLocal", BoolT.INSTANCE
    );


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof AreaT;
    }

    @Override
    public Map<String, Type> fieldTypes() {
        return FIELDS;
    }
}
