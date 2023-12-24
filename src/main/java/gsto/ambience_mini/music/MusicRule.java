package gsto.ambience_mini.music;

import javax.annotation.Nullable;
import java.util.List;

public record MusicRule(@Nullable String dimension, @Nullable String biome, @Nullable String structure, List<String> triggers, @Nullable List<Music> music) {
}
