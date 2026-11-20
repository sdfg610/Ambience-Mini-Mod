package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record Accessor(Expr base, Ident field, int line) implements Expr {
    public Expr withBase(Expr newBase) {
        return new Accessor(newBase, field, line);
    }
}
