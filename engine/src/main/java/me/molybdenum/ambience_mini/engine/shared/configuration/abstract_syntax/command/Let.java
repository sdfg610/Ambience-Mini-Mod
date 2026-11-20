package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;
import org.jetbrains.annotations.Nullable;

public record Let(@Nullable Type type, Ident ident, Expr value, Command body, int line) implements Command {
    public Let withValueAndBody(Expr newValue, Command newBody) {
        return new Let(type, ident, newValue, newBody, line);
    }
}
