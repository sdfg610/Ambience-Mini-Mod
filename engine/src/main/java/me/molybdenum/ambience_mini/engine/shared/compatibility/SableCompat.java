package me.molybdenum.ambience_mini.engine.shared.compatibility;

import java.util.function.Function;

public class SableCompat
{
    public static final String MOD_ID = "sable";

    public static boolean isLoaded = false;


    public static void init(Function<String, Boolean> isModLoaded) {
        isLoaded = isModLoaded.apply(MOD_ID);
    }
}
