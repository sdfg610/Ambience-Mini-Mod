package me.molybdenum.ambience_mini.engine.client.core.monitor.player;

import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.configuration.music_provider.BaseMusicProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;

public record MusicInstance(BaseMusicProvider provider, Music music) {
    public InputStream createStream() throws FileNotFoundException {
        return provider.getMusicStream(music);
    }

    public int getMusicSize() throws FileNotFoundException {
        return provider.getMusicSize(music);
    }
}
