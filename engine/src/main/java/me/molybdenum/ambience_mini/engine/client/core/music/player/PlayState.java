package me.molybdenum.ambience_mini.engine.client.core.music.player;

import org.jetbrains.annotations.Nullable;


public interface PlayState {
    String getPath();
    @Nullable String getTitleOrNull();
    @Nullable String getAuthorOrNull();

    boolean isPlaying();

    long getSamplePosition();
    long getTotalSamples();
    int getSamplesPerSecond();
    long getLoopStart();
    long getLoopEnd();

    default String getTitleOrPath() {
        String title = getTitleOrNull();
        return title == null ? getPath() : title;
    }

    default boolean isLooping() {
        return getLoopStart() >= 0;
    }
}
