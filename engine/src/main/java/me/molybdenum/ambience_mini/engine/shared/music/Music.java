package me.molybdenum.ambience_mini.engine.shared.music;

import me.molybdenum.ambience_mini.engine.shared.music.music_dto.ValueDTO;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record Music(
        @NotNull String path,
        boolean locatedOnServer,
        float volumeAdjustment,
        boolean loop,
        int loopstart,
        int loopend,
        int looplength
) {
    public static final String ARG_GAIN = "gain";  // Float

    public static final String ARG_LOOP = "loop";  // Bool
    public static final String ARG_LOOPSTART = "loopstart";  // Int
    public static final String ARG_LOOPEND = "loopend";  // Int
    public static final String ARG_LOOPLENGTH = "looplength";  // Int


    public Music(
            @NotNull String path,
            float volumeAdjustment,
            boolean loop,
            int loopstart,
            int loopend,
            int looplength
    ) {
        this(
                path,
                false,
                volumeAdjustment,
                loop,
                loopstart,
                loopend,
                looplength
        );
    }


    public boolean doLoop() {
        return loop || (loopstart >= 0 && (loopend >= 0 || looplength >= 0));
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
            put(Music.ARG_LOOPSTART, new ValueDTO.IntDTO(loopstart));
            put(Music.ARG_LOOPEND, new ValueDTO.IntDTO(loopend));
            put(Music.ARG_LOOPLENGTH, new ValueDTO.IntDTO(looplength));
        }};
    }


    @Override
    public @NotNull String toString() {
        return path;
    }
}
