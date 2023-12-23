package gsto.ambience_mini.music;

import javax.annotation.Nullable;
import java.util.List;

public class MusicRule {
    @Nullable public final String dimension;
    @Nullable public final String biome;
    @Nullable public final String structure;
    @Nullable public final String boss;
    public final List<String> triggers;

    public final List<Music> music;


    public MusicRule(@Nullable String dimension, @Nullable String biome, @Nullable String structure, @Nullable String boss, List<String> triggers, List<Music> music) {
        this.dimension = dimension;
        this.biome = biome;
        this.structure = structure;
        this.boss = boss;
        this.triggers = triggers;

        this.music = music;
    }
}
