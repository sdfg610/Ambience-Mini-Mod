package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

public enum ModifierKinds {
    VOLUME("volume")

    ;

    public final String name;

    ModifierKinds(String name) {
        this.name = name;
    }

    public static ModifierKinds ofName(String name) {
        for (var mod : ModifierKinds.values())
            if (mod.name.equals(name))
                return mod;
        return null;
    }
}
