package me.molybdenum.ambience_mini.engine.player.rule.condition;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.BinaryOperators;

import java.util.Objects;

public record BinOpCondition(BinaryOperators op, Condition left, Condition right) implements Condition {
    @Override
    public Object evaluate() {
        return switch (op) {
            case EQ -> Objects.equals(left.evaluate(), right.evaluate());
            case APP_EQ -> ((String)left.evaluate()).contains(((String)right.evaluate()));
            case AND -> ((Boolean)left.evaluate()) && ((Boolean)right.evaluate());
            case OR -> ((Boolean)left.evaluate()) || ((Boolean)right.evaluate());
            case LT -> ((Number)left.evaluate()).doubleValue() < ((Number)right.evaluate()).doubleValue();
        };
    }
}
