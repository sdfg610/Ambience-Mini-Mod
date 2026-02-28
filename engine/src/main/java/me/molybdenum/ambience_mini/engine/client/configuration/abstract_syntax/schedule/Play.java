package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist.Playlist;

public record Play(Playlist playlist, boolean isInstant) implements Schedule { }
