package me.molybdenum.ambience_mini.engine.client.core.gui;

import java.util.function.Supplier;

public abstract class BaseGuiTools {
    private static BaseGuiTools INSTANCE;


    public static BaseGuiTools get() {
        if (INSTANCE == null)
            throw new NullPointerException("The BaseGuiSounds instance is null! Remember to initialize on mod loading");
        return INSTANCE;
    }

    public static void init(Supplier<BaseGuiTools> soundBuilder) {
        if (INSTANCE == null)
            INSTANCE = soundBuilder.get();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API

    // Sounds
    public abstract void PlayMouseClick();

    // Text
    public abstract int getTextWidth(String text);
    public abstract int getLineHeight();

    public abstract String headSubstringByWidth(String str, int width);
    public abstract String tailSubstringByWidth(String str, int width);


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public int getTextHeight(String text) {
        if(text == null || text.isEmpty())
            return 0;
        return getLineHeight() * (1 + (int)text.chars().filter(ch -> ch == '\n').count());
    }
}
