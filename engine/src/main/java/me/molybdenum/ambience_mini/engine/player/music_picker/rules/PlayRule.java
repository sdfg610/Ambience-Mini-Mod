package me.molybdenum.ambience_mini.engine.player.music_picker.rules;

import me.molybdenum.ambience_mini.engine.player.Music;
import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;

import java.util.List;

public record PlayRule(List<Music> playlist, boolean isInstant) implements Rule {
    @Override
    public PlaylistChoice getNext() {
        return new PlaylistChoice(playlist, false, isInstant);
    }
}
