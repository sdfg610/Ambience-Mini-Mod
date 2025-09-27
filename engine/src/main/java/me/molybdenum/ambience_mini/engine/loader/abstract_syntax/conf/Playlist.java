package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play.IdentP;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play.PL;

public record Playlist(IdentP ident, PL playlist, Conf conf) implements Conf {}
