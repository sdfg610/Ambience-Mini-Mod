package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers.ValueList;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.IndexableV;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class ListVal extends Value<ValueList> implements IndexableV
{
    public static final ListVal UNDEFINED = new ListVal();
    public static final ListVal EMPTY = new ListVal(new ValueList());


    public ListVal() {
        super(null);
    }

    public ListVal(Stream<Value<?>> values) {
        super(ValueList.of(values));
    }

    public ListVal(ValueList value) {
        super(value);
    }


    public static ListVal ofStringList(List<String> value) {
        return value == null ? UNDEFINED : new ListVal(value.stream().map(StringVal::new));
    }


    @Override
    public String toStringInner(@NotNull ValueList value) {
        return "[ " + String.join(", ", value.stream().map(Value::toString).toList()) + " ]";
    }

    @Override
    public boolean equals(Value<?> other) {
        return (value == null && other.value == null)
                || (value != null && other instanceof ListVal listVal && listVal.value != null && compare(listVal));
    }

    private boolean compare(ListVal listVal) {
        Iterator<Value<?>> it1 = value.iterator();
        Iterator<Value<?>> it2 = listVal.value.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            if (!it1.next().equals(it2.next()))
                return false;
        }

        return it1.hasNext() == it2.hasNext(); // True when both are false; meaning same length.
    }


    @Override
    public Value<?> getIndex(Value<?> index) {
        var idx = index.asInt();
        return value != null && idx.isPresent()
                ? value.stream().skip(idx.get()).findFirst().orElse(UndefinedVal.INSTANCE)
                : UndefinedVal.INSTANCE;
    }
}
