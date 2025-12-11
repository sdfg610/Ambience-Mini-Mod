package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression;

public record BinaryOp(BinaryOperators op, Expr left, Expr right, int opLine) implements Expr { }
