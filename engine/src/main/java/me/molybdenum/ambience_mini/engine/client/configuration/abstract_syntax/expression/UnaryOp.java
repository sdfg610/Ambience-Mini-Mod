package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression;

public record UnaryOp(UnaryOperators op, Expr expr, int opLine) implements Expr { }
