package me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.LivingT;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.kinds.AccessibleV;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

import static me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.LivingT.*;

public final class LivingVal extends Value<LivingVal.LivingDescriptor> implements AccessibleV {

    public LivingVal() {
        super(null);
    }

    public LivingVal(LivingVal.LivingDescriptor value) {
        super(value);
    }

    public LivingVal(String typeId, float health, float maxHealth) {
        super(new LivingVal.LivingDescriptor(typeId, health, maxHealth));
    }


    @Override
    public String toStringInner(@NotNull LivingVal.LivingDescriptor value) {
        return "Living { " + LivingT.FIELDS.keySet().stream().map(key -> key + "=" + getField(key).toString()).collect(Collectors.joining(", ")) + " }";
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
            default -> UndefinedVal.INSTANCE;
        };
    }

    public record LivingDescriptor(String typeId, float health, float maxHealth) { }
}
