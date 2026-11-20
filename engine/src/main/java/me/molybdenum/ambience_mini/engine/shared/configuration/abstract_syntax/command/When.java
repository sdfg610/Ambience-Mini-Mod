package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;

public record When(Expr condition, Command body, int line) implements Command {
}
