package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play.*;

public record Play(PL playlist, boolean isInstant) implements Shed { }
