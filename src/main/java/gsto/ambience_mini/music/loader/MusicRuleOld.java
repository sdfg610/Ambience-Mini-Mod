package gsto.ambience_mini.music.loader;

import gsto.ambience_mini.music.player.Music;

import javax.annotation.Nullable;
import java.util.List;

public record MusicRuleOld(@Nullable String dimension, @Nullable String biome, @Nullable String structure, List<String> triggers, @Nullable List<Music> music) {
}
