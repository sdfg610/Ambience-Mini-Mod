package me.molybdenum.ambience_mini.engine.client.core.monitor.music_selector.selection;

import me.molybdenum.ambience_mini.engine.shared.music.Music;

import java.util.List;

public record PlaylistSelection(List<Music> playlist, boolean isInstant, int priority) implements Selection {
}
