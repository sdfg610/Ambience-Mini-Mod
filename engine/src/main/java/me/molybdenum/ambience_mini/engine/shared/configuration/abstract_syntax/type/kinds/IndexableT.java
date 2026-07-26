package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;

public interface IndexableT {
    Type indexerType();
    Type outputType();
}
