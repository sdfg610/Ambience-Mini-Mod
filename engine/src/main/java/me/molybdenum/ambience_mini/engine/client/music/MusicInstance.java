package me.molybdenum.ambience_mini.engine.client.music;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;

public record MusicInstance(MusicProvider provider, Music music) {
    public InputStream createStream() throws FileNotFoundException {
        return provider.getMusicStream(music().path());
    }

    public int getMusicSize() throws FileNotFoundException {
        return provider.getMusicSize(music().path());
    }
}
