package me.molybdenum.ambience_mini.engine.client.configuration;

import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Music(String musicPath, float volumeAdjustment)
{
    @Override
    public @NotNull String toString() {
        return musicPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return Float.compare(volumeAdjustment, music.volumeAdjustment) == 0 && Objects.equals(musicPath, music.musicPath);
    }


    public String getExtension() {
        return Utils.getFileExtension(musicPath);
    }

    public float getFractionalAdjustment() {
        return volumeAdjustment / 100f;
    }
}
