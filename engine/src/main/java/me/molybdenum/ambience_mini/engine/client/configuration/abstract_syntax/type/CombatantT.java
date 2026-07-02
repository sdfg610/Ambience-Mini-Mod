package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.kinds.AccessibleT;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class CombatantT extends Type implements AccessibleT
{
    public static final CombatantT INSTANCE = new CombatantT();

    public static final String FIELD_TYPE_ID = "type_id";
    public static final String FIELD_HEALTH = "health";
    public static final String FIELD_MAX_HEALTH = "max_health";
    public static final String FIELD_HEALTH_PERCENT = "health_percent";
    public static final String FIELD_TARGETING_PLAYER = "targeting_player";
    public static final String FIELD_FIGHTING_PLAYER = "fighting_player";

    public static final Map<String, Type> FIELDS = Map.of(
            FIELD_TYPE_ID, StringT.INSTANCE,
            FIELD_HEALTH, FloatT.INSTANCE,
            FIELD_MAX_HEALTH, FloatT.INSTANCE,
            FIELD_HEALTH_PERCENT, FloatT.INSTANCE,
            FIELD_TARGETING_PLAYER, BoolT.INSTANCE,
            FIELD_FIGHTING_PLAYER, BoolT.INSTANCE
    );


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof CombatantT;
    }

    @Override
    public Map<String, Type> fieldTypes() {
        return FIELDS;
    }
}
