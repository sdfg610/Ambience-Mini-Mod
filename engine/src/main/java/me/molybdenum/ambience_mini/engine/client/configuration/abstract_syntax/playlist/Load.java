package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.StringLit;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.FloatLit;

public record Load(StringLit file, FloatLit gain, int line) implements Playlist { }
