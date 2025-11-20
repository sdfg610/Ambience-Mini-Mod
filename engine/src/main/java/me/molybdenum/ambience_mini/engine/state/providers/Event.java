package me.molybdenum.ambience_mini.engine.state.providers;

import me.molybdenum.ambience_mini.engine.loader.interpreter.values.BoolVal;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Event {
    public final String name;

    private final Supplier<BoolVal> _isActive;
    private final BiConsumer<Event, BoolVal> _onFired;


    public Event(String name, Supplier<BoolVal> isActive, BiConsumer<Event, BoolVal> onFired) {
        this.name = name;
        this._isActive = isActive;
        this._onFired = onFired;
    }


    public BoolVal isActive() {
        BoolVal result = _isActive.get();
        _onFired.accept(this, result);
        return result;
    }
}
