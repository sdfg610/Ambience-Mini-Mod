package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

import me.molybdenum.ambience_mini.engine.state.providers.Event;

import java.util.function.Supplier;

public class EventCondition implements Condition {
    Supplier<Object> _isActive;

    public EventCondition(Event event) {
        _isActive = event::isActive;
    }

    @Override
    public Object evaluate() {
        return _isActive.get();
    }
}
