package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.BoolVal;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.Value;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Property {
    public final String name;
    public final Type type;

    private final Supplier<Value> _getter;
    private final BiConsumer<Property, Value> _onFired;

    private Value _latestValue;


    public Property(String name, Type type, Supplier<Value> getter, BiConsumer<Property, Value> onFired) {
        this.name = name;
        this.type = type;
        this._getter = getter;
        this._onFired = onFired;

        this._latestValue = Value.ofType(type);
    }


    public Value getValue() {
        Value result = _getter.get();
        if (result != null) _latestValue = result;
        _onFired.accept(this, _latestValue);
        return _latestValue;
    }
}
