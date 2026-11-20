package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record TerneryOp(Expr expr1, Expr expr2, Expr expr3, int line) implements Expr {
    @Override
    public int line() {
        return line;
    }


    public Expr withExprs(Expr expr1, Expr expr2, Expr expr3) {
        return new TerneryOp(expr1, expr2, expr3, line);
    }
}
