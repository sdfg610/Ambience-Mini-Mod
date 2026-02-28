package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule;

public record Interrupt(Schedule body, int line) implements Schedule {
}
