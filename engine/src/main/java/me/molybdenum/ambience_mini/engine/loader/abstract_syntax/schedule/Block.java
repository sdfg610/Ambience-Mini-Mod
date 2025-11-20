package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.schedule;

import java.util.List;

public record Block(List<Schedule> body) implements Schedule { }
