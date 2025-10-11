package me.molybdenum.ambience_mini.engine.player.music_picker;

import me.molybdenum.ambience_mini.engine.player.Music;
import java.util.List;

public record PlaylistChoice(List<Music> playlist, boolean isInterrupt, boolean isInstant) {
    public PlaylistChoice asInterrupt() {
        return new PlaylistChoice(playlist, true, isInstant);
    }
}
