package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;

public record PlaylistInstance(Expr expr) implements ServerPlaylists { }
