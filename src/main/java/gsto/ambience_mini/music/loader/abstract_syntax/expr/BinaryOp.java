package gsto.ambience_mini.music.loader.abstract_syntax.expr;

public record BinaryOp(BinaryOperators op, Expr left, Expr right) implements Expr { }
