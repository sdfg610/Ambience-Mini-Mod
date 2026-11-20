package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record UnaryOp(UnaryOperators op, Expr expr, int opLine) implements Expr {
    @Override
    public int line() {
        return opLine;
    }

    public UnaryOp withExpr(Expr newExpr) {
        return new UnaryOp(op, newExpr, opLine);
    }
}
