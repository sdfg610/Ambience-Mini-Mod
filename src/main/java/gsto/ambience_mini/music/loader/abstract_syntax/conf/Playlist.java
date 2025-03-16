package gsto.ambience_mini.music.loader.abstract_syntax.conf;

import gsto.ambience_mini.music.loader.abstract_syntax.play.IdentP;
import gsto.ambience_mini.music.loader.abstract_syntax.play.PL;

public record Playlist(IdentP ident, PL playlist, Conf conf) implements Conf {}
