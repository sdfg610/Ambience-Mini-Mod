package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.ValueLit;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.Value;

import java.util.function.Consumer;
import java.util.function.Function;

public class ExprOptimizeResult {
    private final Expr expr;
    private final Type type;
    private final Boolean isConst;

    public ExprOptimizeResult(Expr expr, Type type, Boolean isConst) {
        this.expr = expr;
        this.type = type;
        this.isConst = isConst;

        if (type == null && expr != null)
            throw new RuntimeException("Ill-typed expression cannot be well-defined");
        else if (isConst == null && expr != null)
            throw new RuntimeException("Well-defined expression must have known constness");
    }


    public Expr expr() {
        return expr;
    }

    public Type type() {
        return type;
    }

    public Boolean baseIsConst() {
        return isConst;
    }


    public void ifTypeDefined(Consumer<Type> onWellDefined) {
        if (type != null)
            onWellDefined.accept(type);
    }

    public <T> T ifTypeDefinedOrNull(Function<Type, T> onTypeDefined) {
        return type != null ? onTypeDefined.apply(type) : null;
    }


    public boolean isWellDefined() {
        return expr != null;
    }

    public <T> T ifWellDefinedOrNull(Function<Expr, T> onWellDefined) {
        return isWellDefined() ? onWellDefined.apply(expr) : null;
    }


    public boolean isConstant() {
        return isConst != null && isConst;
    }

    public boolean isNotConstant() {
        return isConst != null && !isConst;
    }



    public boolean isStaticallyEvaluable() {
        return isConstant() && isWellDefined();
    }

    public Value<?> getValue() {
        return ((ValueLit) expr).value();
    }
}