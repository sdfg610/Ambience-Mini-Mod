package gsto.ambience_mini.music.loader.abstract_syntax.play;

import gsto.ambience_mini.music.loader.abstract_syntax.expr.FloatV;
import gsto.ambience_mini.music.loader.abstract_syntax.expr.StringV;

public record Load(StringV file, FloatV gain) implements PL { }
