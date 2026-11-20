package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

public record Interrupt(Command body, int line) implements Command {
}
