package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression;

public record QuantifierOp(Quantifiers quantifier, String identifier, Expr list, Expr condition) implements Expr { }
