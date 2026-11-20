package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record GetEvent(Ident eventName) implements Expr {
    @Override
    public int line() {
        return eventName.line();
    }
}
