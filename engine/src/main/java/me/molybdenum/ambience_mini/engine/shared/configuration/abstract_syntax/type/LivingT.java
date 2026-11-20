package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds.AccessibleT;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class LivingT extends Type implements AccessibleT
{
    public static final LivingT INSTANCE = new LivingT();

    public static final String FIELD_TYPE_ID = "type_id";
    public static final String FIELD_HEALTH = "health";
    public static final String FIELD_MAX_HEALTH = "max_health";

    public static final Map<String, Type> FIELDS = Map.of(
            FIELD_TYPE_ID, StringT.INSTANCE,
            FIELD_HEALTH, FloatT.INSTANCE,
            FIELD_MAX_HEALTH, FloatT.INSTANCE
    );


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof LivingT;
    }

    @Override
    public Map<String, Type> fieldTypes() {
        return FIELDS;
    }
}
