package me.molybdenum.ambience_mini.engine.client.configuration;

import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Music(String path, float volumeAdjustment, boolean loop)
{
    @Override
    public @NotNull String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return Float.compare(volumeAdjustment, music.volumeAdjustment) == 0 && Objects.equals(path, music.path);
    }


    public String getExtension() {
        return Utils.getFileExtension(path);
    }

    public float getCorrectedAdjustment() {
        return (volumeAdjustment * 2f) / 100f;
    }
}
