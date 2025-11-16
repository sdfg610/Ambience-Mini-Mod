package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.BinaryOperators;
import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;

import java.util.Objects;

public record BinOpCondition(BinaryOperators op, Condition left, Condition right) implements Condition {
    @Override
    public Object evaluate(VarEnv env) {
        return switch (op) {
            case EQ -> Objects.equals(left.evaluate(env), right.evaluate(env));
            case APP_EQ -> ((String)left.evaluate(env)).contains(((String)right.evaluate(env)));
            case AND -> ((Boolean)left.evaluate(env)) && ((Boolean)right.evaluate(env));
            case OR -> ((Boolean)left.evaluate(env)) || ((Boolean)right.evaluate(env));
            case LT -> ((Number)left.evaluate(env)).doubleValue() < ((Number)right.evaluate(env)).doubleValue();
        };
    }
}
