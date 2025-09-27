package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.FloatV;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.StringV;

public record Load(StringV file, FloatV gain) implements PL { }
