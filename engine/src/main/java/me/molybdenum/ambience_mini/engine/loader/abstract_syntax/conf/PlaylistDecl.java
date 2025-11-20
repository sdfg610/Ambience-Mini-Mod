package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.playlist.IdentP;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.playlist.Playlist;

public record PlaylistDecl(IdentP ident, Playlist playlist, Config config) implements Config {}
