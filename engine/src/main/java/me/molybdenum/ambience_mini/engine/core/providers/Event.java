package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.BoolVal;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Event {
    public final String name;

    private final Supplier<BoolVal> _isActive;
    private final BiConsumer<Event, BoolVal> _onFired;

    private BoolVal _latestValue = new BoolVal(false);


    public Event(String name, Supplier<BoolVal> isActive, BiConsumer<Event, BoolVal> onFired) {
        this.name = name;
        this._isActive = isActive;
        this._onFired = onFired;
    }


    public BoolVal isActive() {
        BoolVal result = _isActive.get();
        if (result != null) _latestValue = result;
        _onFired.accept(this, _latestValue);
        return _latestValue;
    }
}
