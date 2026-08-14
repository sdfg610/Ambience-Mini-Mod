package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule.Schedule;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.ServerPlaylists;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Config(
        List<GlobalDecl> declarations,
        @Nullable ServerPlaylists serverPlaylists,
        @Nullable Schedule schedule
) { }
