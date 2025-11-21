package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.config;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist.IdentP;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist.Playlist;

public record PlaylistDecl(IdentP ident, Playlist playlist, Config config) implements Config {}
