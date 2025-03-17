package gsto.ambience_mini.music.player.rule;

import gsto.ambience_mini.music.player.rule.condition.Condition;


public record WhenRule(Condition condition, Rule subRule) implements Rule {
    @Override
    public NextMusic getNext() {
        return ((Boolean)condition.evaluate())
                ? subRule.getNext()
                : null;

    }
}
