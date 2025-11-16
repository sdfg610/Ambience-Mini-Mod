package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;

public interface Condition {
    Object evaluate(VarEnv env);
}
