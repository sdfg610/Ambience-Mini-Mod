package me.molybdenum.ambience_mini.engine.shared.music;

import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;

public record Music(@NotNull String path, boolean locatedOnServer, float volumeAdjustment, boolean loop)
{
    public Music(@NotNull String path, float volumeAdjustment, boolean loop) {
        this(path, false, volumeAdjustment, loop);
    }


    public String getExtension() {
        return Utils.getFileExtension(path);
    }

    public float getCorrectedVolumeAdjustment() {
        return (volumeAdjustment * 2f) / 100f;
    }


    @Override
    public @NotNull String toString() {
        return path;
    }
}
