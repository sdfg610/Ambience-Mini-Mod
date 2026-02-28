package me.molybdenum.ambience_mini.engine.shared.compatibility;

import java.util.function.Function;

public class CompatManager {
    public static void init(Function<String, Boolean> isModLoaded) {
        EssentialCompat.init(isModLoaded);
    }
}
