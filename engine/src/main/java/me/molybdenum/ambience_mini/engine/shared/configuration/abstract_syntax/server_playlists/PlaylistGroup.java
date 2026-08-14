package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists;

import java.util.HashMap;

public record PlaylistGroup(HashMap<String, ServerPlaylists> body) implements ServerPlaylists {
}
