package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;

public class TypeBinding {
    public final Type type;
    public final int line;
    private boolean isUsed;
    private boolean isConst;
    private boolean isWellDefined;


    public TypeBinding(Type type, int line) {
        this.type = type;
        this.line = line;
    }


    public void markAsUsed() {
        isUsed = true;
    }

    public boolean getIsUsed() {
        return isUsed;
    }


    public void markAsConst() {
        isConst = true;
    }

    public boolean getIsConst() {
        return isConst;
    }


    public void markAsWellDefined() {
        isWellDefined = true;
    }

    public boolean getIsWellDefined() {
        return isWellDefined;
    }


    public boolean isStaticallyEvaluable() {
        return isConst && isWellDefined;
    }
}
