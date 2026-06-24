package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.CombatantT;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.AccessibleV;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

import static me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.CombatantT.*;

public final class CombatantVal extends Value<CombatantVal.CombatantDescriptor> implements AccessibleV {

    public CombatantVal() {
        super(null);
    }

    public CombatantVal(CombatantVal.CombatantDescriptor value) {
        super(value);
    }

    public CombatantVal(String typeId, Float health, Float maxHealth) {
        super(new CombatantVal.CombatantDescriptor(typeId, health, maxHealth));
    }


    @Override
    public String toStringInner(@NotNull CombatantVal.CombatantDescriptor value) {
        return "Combatant { " + CombatantT.FIELDS.keySet().stream().map(key -> key + "=" + getField(key).toString()).collect(Collectors.joining(", ")) + " }";
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }

    @Override
    public Value<?> getField(String field) {
        return switch (field) {
            case FIELD_TYPE_ID -> new StringVal(value.typeId);
            case FIELD_HEALTH -> new FloatVal(value.health);
            case FIELD_MAX_HEALTH -> new FloatVal(value.maxHealth);
            case FIELD_HEALTH_PERCENT -> new FloatVal(
                    value.health == null || value.maxHealth == null ? null : (value.health / value.maxHealth) * 100
            );
            default -> UndefinedVal.INSTANCE;
        };
    }

    public record CombatantDescriptor(String typeId, Float health, Float maxHealth) { } // Using boxed types to allow nullable health and maxHealth in case a non-living entity enters this list (and to avoid changing a ton of other things)
}
