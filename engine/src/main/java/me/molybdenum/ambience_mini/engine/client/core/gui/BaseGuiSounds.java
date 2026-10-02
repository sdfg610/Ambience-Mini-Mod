package me.molybdenum.ambience_mini.engine.client.core.gui;

import java.util.function.Supplier;

public abstract class BaseGuiSounds {
    private static BaseGuiSounds INSTANCE;


    public abstract void PlayMouseClick();


    public static BaseGuiSounds get() {
        if (INSTANCE == null)
            throw new NullPointerException("The BaseGuiSounds instance is null! Remember to initialize on mod loading");
        return INSTANCE;
    }

    public static void init(Supplier<BaseGuiSounds> soundBuilder) {
        if (INSTANCE == null)
            INSTANCE = soundBuilder.get();
    }
}
