package me.molybdenum.ambience_mini.engine.player.music_picker.rules;

import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;

public interface Rule {
    PlaylistChoice getNext(VarEnv env);
}
