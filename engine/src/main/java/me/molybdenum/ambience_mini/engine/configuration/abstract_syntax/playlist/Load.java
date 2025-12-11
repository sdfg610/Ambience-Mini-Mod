package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression.FloatLit;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression.StringLit;

public record Load(StringLit file, FloatLit gain, int line) implements Playlist { }
