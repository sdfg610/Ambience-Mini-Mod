package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record StringLit(String value, int line) implements Expr { }
