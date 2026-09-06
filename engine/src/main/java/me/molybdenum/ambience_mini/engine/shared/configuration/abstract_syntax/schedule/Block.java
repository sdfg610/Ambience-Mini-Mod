package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule;

import java.util.List;

public record Block(List<Schedule> body, int line) implements Schedule {
    public Block withBody(List<Schedule> newBody) {
        return new Block(newBody, line);
    }
}
