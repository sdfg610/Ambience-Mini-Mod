package me.molybdenum.ambience_mini.engine.shared.music;

import me.molybdenum.ambience_mini.engine.shared.music.music_dto.ValueDTO;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record Music(@NotNull String path, boolean locatedOnServer, float volumeAdjustment, boolean loop)
{
    public static final String ARG_GAIN = "gain";  // Float
    public static final String ARG_LOOP = "loop";  // Bool


    public Music(@NotNull String path, float volumeAdjustment, boolean loop) {
        this(path, false, volumeAdjustment, loop);
    }


    public String getExtension() {
        return Utils.getFileExtension(path);
    }

    public float getCorrectedVolumeAdjustment() {
        return (volumeAdjustment * 2f) / 100f;
    }


    public Map<String, ValueDTO> getMusicFlags() {
        return new HashMap<>() {{
            put(Music.ARG_GAIN, new ValueDTO.FloatDTO(volumeAdjustment));
            put(Music.ARG_LOOP, new ValueDTO.BoolDTO(loop));
        }};
    }


    @Override
    public @NotNull String toString() {
        return path;
    }
}
