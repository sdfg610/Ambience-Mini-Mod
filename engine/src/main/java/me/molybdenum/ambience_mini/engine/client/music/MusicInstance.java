package me.molybdenum.ambience_mini.engine.client.music;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public record MusicInstance(MusicProvider provider, Music music) {
    public Path getMusicPath() {
        return provider.getFullPath(music.path());
    }

    public BufferedInputStream createStream() {
        try {
            return provider.getMusicStream(music().path());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
