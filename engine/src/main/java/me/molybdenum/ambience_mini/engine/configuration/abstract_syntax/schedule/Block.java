package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule;

import java.util.List;

public record Block(List<Schedule> body) implements Schedule { }
