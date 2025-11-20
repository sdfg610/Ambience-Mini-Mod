package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expression;

public record QuantifierOp(Quantifiers quantifier, String identifier, Expr list, Expr condition) implements Expr { }
