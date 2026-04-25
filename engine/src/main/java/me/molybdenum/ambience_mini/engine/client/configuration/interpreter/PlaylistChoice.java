package me.molybdenum.ambience_mini.engine.client.configuration.interpreter;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;

import java.util.List;

public record PlaylistChoice(List<Music> playlist, boolean isInstant, int priority) {
}
