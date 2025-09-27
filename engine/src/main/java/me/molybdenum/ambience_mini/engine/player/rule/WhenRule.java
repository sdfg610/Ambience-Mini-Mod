package me.molybdenum.ambience_mini.engine.player.rule;

import me.molybdenum.ambience_mini.engine.player.rule.condition.Condition;


public record WhenRule(Condition condition, Rule subRule) implements Rule {
    @Override
    public PlaylistChoice getNext() {
        return ((Boolean)condition.evaluate())
                ? subRule.getNext()
                : null;

    }
}
