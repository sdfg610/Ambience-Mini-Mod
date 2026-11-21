package me.molybdenum.ambience_mini.engine.client.core;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class BaseSynchroniser {
    // Pretty high since main thread blocks if the window is being dragged and the music player should not crash from being dragged a bit.
    protected static final int MAIN_THREAD_TIMEOUT_MILLIS = 10_000;

    private static BaseSynchroniser INSTANCE;


    public static BaseSynchroniser get() {
        if (INSTANCE == null)
            throw new NullPointerException("The BaseSynchroniser instance is null! Remember to initialize on mod loading");
        return INSTANCE;
    }

    public static void init(Supplier<BaseSynchroniser> builder) {
        if (INSTANCE == null)
            INSTANCE = builder.get();
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Minecraft thread synchronisation
    @Nullable
    public abstract <T> T syncToMain(Supplier<T> task);

    @Nullable
    public <T> T syncToMainIf(boolean condition, Supplier<T> task) {
        return condition ? syncToMain(task) : task.get();
    }
}
