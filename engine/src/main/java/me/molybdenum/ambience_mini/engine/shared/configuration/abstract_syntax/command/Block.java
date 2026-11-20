package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

import java.util.List;

public record Block(List<Command> body, int line) implements Command {
    public Block withBody(List<Command> newBody) {
        return new Block(newBody, line);
    }
}
