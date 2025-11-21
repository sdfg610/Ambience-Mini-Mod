package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist.*;

public record Play(Playlist playlist, boolean isInstant) implements Schedule { }
