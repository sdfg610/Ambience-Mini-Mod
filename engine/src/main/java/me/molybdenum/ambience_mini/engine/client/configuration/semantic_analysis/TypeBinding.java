package me.molybdenum.ambience_mini.engine.client.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.Type;

public class TypeBinding {
    public final Type type;
    public final int line;
    private boolean isUsed;


    public TypeBinding(Type type, int line) {
        this.type = type;
        this.line = line;
    }


    public void markIsUsed() {
        isUsed = true;
    }

    public boolean getIsUsed() {
        return isUsed;
    }
}
