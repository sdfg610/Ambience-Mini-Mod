package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public record Ident(String value, int line) implements Expr { }
