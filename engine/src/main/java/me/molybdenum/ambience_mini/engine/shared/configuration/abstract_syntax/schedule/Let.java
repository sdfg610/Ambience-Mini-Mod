package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;
import org.jetbrains.annotations.Nullable;

public record Let(@Nullable Type type, Ident ident, Expr value, Schedule body, int line) implements Schedule {
    public Let withValueAndBody(Expr newValue, Schedule newBody) {
        return new Let(type, ident, newValue, newBody, line);
    }
}
