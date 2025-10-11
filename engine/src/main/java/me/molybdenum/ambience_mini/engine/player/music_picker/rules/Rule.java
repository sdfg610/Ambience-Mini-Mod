package me.molybdenum.ambience_mini.engine.player.music_picker.rules;

import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;

public interface Rule {
    PlaylistChoice getNext();
}
