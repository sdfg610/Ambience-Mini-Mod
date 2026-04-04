package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.AreaT;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.AccessibleV;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class AreaVal extends Value<Area> implements AccessibleV {

    public AreaVal() {
        super(null);
    }

    public AreaVal(Area value) {
        super(value);
    }


    @Override
    public String toStringInner(@NotNull Area value) {
        return "Area { " + AreaT.FIELDS.keySet().stream().map(key -> key + "=" + getField(key).toString()).collect(Collectors.joining(", ")) + " }";
    }

    @Override
    public boolean equals(Value<?> other) {
        return (isUndefined() && other.isUndefined())
                || (value != null && other instanceof AreaVal areaVal && value.id == areaVal.value.id); // Areas with the same id *SHOULD* very well be the same.
    }


    @Override
    public Value<?> getField(String field) {
        return switch (field) {
            case "name" -> new StringVal(value.name);
            case "owner" -> new StringVal(value.owner.getOwnerIdIfOwned());
            case "isShared" -> new BoolVal(value.owner.isShared());
            case "isLocal" -> new BoolVal(value.owner.isLocal());
            default -> UndefinedVal.INSTANCE;
        };
    }
}
