package me.molybdenum.ambience_mini.engine.server.core.setup;

import java.util.function.Supplier;

public abstract class BaseServerConfig {
    // Server music
    public final Supplier<Integer> musicCacheMaxMemory;
    public final Supplier<Integer> musicCacheUnusedThreshold;
    public final Supplier<Integer> musicCacheCleanupInterval;

    // Areas
    public final Supplier<Boolean> enableAreas;

    // Flags
    public final Supplier<Boolean> enableFlags;
    public final Supplier<Integer> flagsSaveInterval;


    protected BaseServerConfig() {
        preSetup();

        // Server music
        musicCacheMaxMemory = makeIntOption(
                "The maximum amount of memory, measured in megabytes (10^6), the server may use to cache server-located music being sent to clients. [Default: 500]",
                "Music_Cache_Max_Memory",
                1000, 100, 10_000
        );

        musicCacheUnusedThreshold = makeIntOption(
                "After this many milliseconds of not being accessed, a cached, server-located music is considered unused. [Default: 10000]",
                "Music_Cache_Unused_Threshold",
                10_000, 2000, 60_000
        );

        musicCacheCleanupInterval = makeIntOption(
                "The time in milliseconds between removing unused server-located music from the cache. [Default: 10000]",
                "Music_Cache_Cleanup_Interval",
                10_000, 2000, 60_000
        );

        // Areas
        enableAreas = makeBoolOption(
                "Whether or not the server allows players to store areas on the server.",
                "Enable_Areas",
                true
        );

        // Flags
        enableFlags = makeBoolOption(
                "Whether or not the server allows the use of flag commands and the $flags property.",
                "Enable_Flags",
                true
        );

        flagsSaveInterval = makeIntOption(
                "The time in milliseconds between auto-saving flags. [Default: 60000]",
                "Flags_Save_Interval",
                60_000, 10_000, 600_000
        );

        postSetup();
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract Supplier<Boolean> makeBoolOption(String comment, String name, boolean defaultValue);
    protected abstract Supplier<Integer> makeIntOption(String comment, String name, int defaultValue, int min, int max);

    protected abstract void preSetup();
    protected abstract void postSetup();
}
