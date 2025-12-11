package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression;

public record QuantifierOp(Quantifiers quantifier, IdentE identifier, Expr list, Expr condition, int inLine, int whereLine) implements Expr { }
