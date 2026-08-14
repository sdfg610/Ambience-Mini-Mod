package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record QuantifierOp(Quantifiers quantifier, Ident identifier, Expr list, Expr condition, int inLine, int whereLine) implements Expr {
    @Override
    public int line() {
        return inLine;
    }
}
