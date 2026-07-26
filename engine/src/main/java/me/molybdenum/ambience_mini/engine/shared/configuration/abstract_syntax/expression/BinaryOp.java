package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record BinaryOp(BinaryOperators op, Expr left, Expr right, int line) implements Expr { }
