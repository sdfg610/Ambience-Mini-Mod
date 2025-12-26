package me.molybdenum.ambience_mini.engine.configuration.interpreter.values;

import java.util.List;

public final class ListVal extends Value
{
    public static final ListVal EMPTY = new ListVal(List.of());

    public final List<Value> value;


    public ListVal(List<Value> value) {
        this.value = value;
    }


    public static ListVal ofStringList(List<String> value) {
        return new ListVal(value.stream().map(StringVal::new).map(v ->(Value)v).toList());
    }


    public String toString() {
        return "[ " + String.join(", ", value.stream().map(Value::toString).toList()) + " ]";
    }
}
