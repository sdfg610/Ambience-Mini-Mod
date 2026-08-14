package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record BoolLit(boolean value, int line) implements Expr { }
