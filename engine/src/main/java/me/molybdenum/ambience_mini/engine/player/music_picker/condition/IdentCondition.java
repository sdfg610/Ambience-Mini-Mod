package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;

public record IdentCondition(String ident) implements Condition {
    @Override
    public Object evaluate(VarEnv env) {
        return env.lookup(ident);
    }
}
