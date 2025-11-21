package me.molybdenum.ambience_mini.engine.configuration.interpreter;

import me.molybdenum.ambience_mini.engine.configuration.Music;

import java.util.List;

public record PlaylistChoice(List<Music> playlist, boolean isInterrupt, boolean isInstant) {
    public PlaylistChoice asInterrupt() {
        return new PlaylistChoice(playlist, true, isInstant);
    }
}
