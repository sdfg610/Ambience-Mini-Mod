package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression;

public record Accessor(Expr base, IdentE field, int line) implements Expr {
}
