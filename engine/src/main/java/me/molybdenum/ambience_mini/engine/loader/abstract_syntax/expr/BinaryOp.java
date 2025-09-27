package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr;

public record BinaryOp(BinaryOperators op, Expr left, Expr right) implements Expr { }
