package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.selection;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;

import java.util.List;

public record PlaylistSelection(List<Music> playlist, boolean isInstant, int priority) implements Selection {
}
