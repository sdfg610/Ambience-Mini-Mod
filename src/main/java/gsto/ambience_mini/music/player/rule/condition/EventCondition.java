package gsto.ambience_mini.music.player.rule.condition;

import gsto.ambience_mini.music.state.Event;

public record EventCondition(Event event) implements Condition {
    @Override
    public Object evaluate() {
        return event.isActive();
    }
}
