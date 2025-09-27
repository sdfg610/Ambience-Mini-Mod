package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed.Shed;

public record Schedule(Shed schedule) implements Conf { }
