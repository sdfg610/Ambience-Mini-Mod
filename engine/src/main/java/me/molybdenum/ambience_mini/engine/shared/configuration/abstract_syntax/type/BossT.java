package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds.AccessibleT;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class BossT extends Type implements AccessibleT
{
    public static final BossT INSTANCE = new BossT();

    public static final String FIELD_ID = "id";
    public static final String FIELD_HEALTH_PERCENT = "health_percent";


    public static final Map<String, Type> FIELDS = Map.of(
            FIELD_ID, StringT.INSTANCE,
            FIELD_HEALTH_PERCENT, FloatT.INSTANCE
    );


    @Override
    protected boolean equalToInternal(@NotNull Type other) {
        return other instanceof BossT;
    }

    @Override
    public Map<String, Type> fieldTypes() {
        return FIELDS;
    }
}
