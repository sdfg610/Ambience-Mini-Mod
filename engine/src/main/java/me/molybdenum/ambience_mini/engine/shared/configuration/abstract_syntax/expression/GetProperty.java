package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record GetProperty(Ident propertyName) implements Expr {
    @Override
    public int line() {
        return propertyName.line();
    }
}
