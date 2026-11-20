package me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.BossT;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.CombatantT;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.kinds.AccessibleV;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

import static me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.CombatantT.*;

public final class BossVal extends Value<BossVal.BossDescriptor> implements AccessibleV {

    public BossVal() {
        super(null);
    }

    public BossVal(BossVal.BossDescriptor value) {
        super(value);
    }

    public BossVal(String typeId, float healthPercent) {
        super(new BossVal.BossDescriptor(typeId, healthPercent));
    }


    @Override
    public String toStringInner(@NotNull BossVal.BossDescriptor value) {
        return "Combatant { " + BossT.FIELDS.keySet().stream().map(key -> key + "=" + getField(key).toString()).collect(Collectors.joining(", ")) + " }";
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }

    @Override
    public Value<?> getField(String field) {
        return switch (field) {
            case FIELD_TYPE_ID -> new StringVal(value.typeId);
            case FIELD_HEALTH_PERCENT -> new FloatVal(value.healthPercent);
            default -> UndefinedVal.INSTANCE;
        };
    }

    public record BossDescriptor(String typeId, float healthPercent) { }
}
