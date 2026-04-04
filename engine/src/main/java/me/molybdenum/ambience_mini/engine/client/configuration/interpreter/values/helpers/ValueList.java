package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class ValueList extends ArrayList<Value<?>> {
    public ValueList() { }

    public ValueList(@NotNull Collection<Value<?>> c) {
        super(c);
    }

    public static ValueList of(Stream<Value<?>> values) {
        ValueList lst = new ValueList();
        values.forEach(lst::add);
        return lst;
    }
}
