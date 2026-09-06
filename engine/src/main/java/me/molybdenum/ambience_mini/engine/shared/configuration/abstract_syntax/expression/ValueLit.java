package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.Value;

public record ValueLit(Value<?> value) implements Expr {
    @Override
    public int line() {
        return -1;
    }
}
