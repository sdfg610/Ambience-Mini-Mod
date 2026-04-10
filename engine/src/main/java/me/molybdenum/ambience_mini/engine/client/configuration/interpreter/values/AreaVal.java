package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.AreaT;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.AccessibleV;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

public final class AreaVal extends Value<AreaVal.AreaDescriptor> implements AccessibleV {

    public AreaVal() {
        super(null);
    }

    public AreaVal(AreaDescriptor value) {
        super(value);
    }

    public AreaVal(Area area) {
        super(new AreaDescriptor(area));
    }


    @Override
    public String toStringInner(@NotNull AreaDescriptor value) {
        return "Area { " + AreaT.FIELDS.keySet().stream().map(key -> key + "=" + getField(key).toString()).collect(Collectors.joining(", ")) + " }";
    }

    @Override
    public boolean equals(Value<?> other) {
        return Objects.equals(value, other.value);
    }


    @Override
    public Value<?> getField(String field) {
        return switch (field) {
            case "name" -> new StringVal(value.name);
            case "dimension" -> new StringVal(value.dimension);
            case "owner" -> new StringVal(value.ownerId);
            case "isShared" -> new BoolVal(value.isShared);
            case "isLocal" -> new BoolVal(value.isLocal);
            default -> UndefinedVal.INSTANCE;
        };
    }


    public record AreaDescriptor(String name, String dimension, String ownerId, Boolean isShared, Boolean isLocal) {
        public AreaDescriptor(Area area) {
            this(area.name, area.dimension, area.owner.getOwnerIdIfOwned(), area.owner.isShared(), area.owner.isLocal());
        }
    }
}
