package me.molybdenum.ambience_mini.engine.player.music_picker.rules;

import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;
import me.molybdenum.ambience_mini.engine.player.music_picker.condition.Condition;


public record WhenRule(Condition condition, Rule subRule) implements Rule {
    @Override
    public PlaylistChoice getNext(VarEnv env) {
        return ((Boolean)condition.evaluate(VarEnv.empty()))
                ? subRule.getNext(env)
                : null;
    }
}
