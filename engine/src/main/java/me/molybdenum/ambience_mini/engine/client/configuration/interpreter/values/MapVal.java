package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers.ValueMap;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.IndexableV;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public final class MapVal extends Value<ValueMap> implements IndexableV
{
    public static final MapVal UNDEFINED = new MapVal();


    public MapVal() {
        super(null);
    }

    public MapVal(ValueMap value) {
        super(value);
    }


    @Override
    public String toStringInner(@NotNull ValueMap value) {
        return "{ " + String.join(", ", value.entrySet().stream().map(entry -> formatPair(entry.getKey(), entry.getValue())).toList()) + " }";
    }

    private String formatPair(Value<?> key, Value<?> value) {
        return key + ": " + value;
    }


    @Override
    public boolean equals(Value<?> other) {
        return (value == null && other.value == null)
                || (value != null && other instanceof MapVal mapVal && mapVal.value != null && compare(mapVal));
    }

    private boolean compare(MapVal mapVal) {
        Iterator<Map.Entry<Value<?>, Value<?>>> it1 = value.entrySet().iterator();
        Iterator<Map.Entry<Value<?>, Value<?>>> it2 = mapVal.value.entrySet().iterator();

        while (it1.hasNext() && it2.hasNext()) {
            var e1 = it1.next();
            var e2 = it2.next();
            if (!e1.getKey().equals(e2.getKey()) || !e1.getValue().equals(e2.getValue()))
                return false;
        }

        return it1.hasNext() == it2.hasNext(); // True when both are false; meaning same length.
    }


    @Override
    public Value<?> getIndex(Value<?> indexer) {
        var val = value != null ? value.get(indexer) : null;
        return val != null ? val : UndefinedVal.INSTANCE;
    }
}
