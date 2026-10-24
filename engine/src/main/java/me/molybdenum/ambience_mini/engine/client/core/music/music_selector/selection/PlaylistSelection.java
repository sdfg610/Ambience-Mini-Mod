package me.molybdenum.ambience_mini.engine.client.core.music.music_selector.selection;

import me.molybdenum.ambience_mini.engine.shared.core.music.Music;

import java.util.List;

public record PlaylistSelection(List<Music> playlist, boolean isInstant, int priority, int line) implements Selection {
}
