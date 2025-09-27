package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed;

import java.util.List;

public record Block(List<Shed> body) implements Shed { }
