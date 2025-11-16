package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.Quantifiers;
import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;

import java.util.List;
import java.util.function.Predicate;

public record QuanOpCondition(
        Quantifiers quantifier,
        String ident,
        Condition list,
        Condition test
) implements Condition {
    @Override
    public Object evaluate(VarEnv env) {
        List<?> lst = (List<?>)list.evaluate(env);
        Predicate<Object> evaluator = elem -> (Boolean)test.evaluate(env.enterScope().bind(ident, elem));
        return switch (quantifier) {
            case ANY -> lst.stream().anyMatch(evaluator);
            case ALL -> lst.stream().allMatch(evaluator);
        };
    }
}
