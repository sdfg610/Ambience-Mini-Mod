package gsto.ambience_mini.music.player.rule.condition;

import gsto.ambience_mini.music.state.Property;

public record PropertyCondition(Property property) implements Condition {
    @Override
    public Object evaluate() {
        return property.getValue();
    }
}
