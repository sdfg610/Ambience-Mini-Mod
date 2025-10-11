package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

public record ValueCondition(Object value) implements Condition {
    @Override
    public Object evaluate() {
        return value;
    }
}
