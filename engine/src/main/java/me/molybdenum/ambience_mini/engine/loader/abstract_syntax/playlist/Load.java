package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.playlist;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expression.FloatLit;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expression.StringLit;

public record Load(StringLit file, FloatLit gain) implements Playlist { }
