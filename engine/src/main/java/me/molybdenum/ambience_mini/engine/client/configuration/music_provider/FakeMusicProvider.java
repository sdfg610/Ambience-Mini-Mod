package me.molybdenum.ambience_mini.engine.client.configuration.music_provider;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class FakeMusicProvider implements MusicProvider {
    @Override
    public boolean exists(String musicPath) {
        return true;
    }

    @Override
    public InputStream getMusicStream(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get a music stream!");
    }

    @Override
    public int getMusicSize(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get a music size!");
    }

    @Override
    public Path getFullPath(String musicPath) {
        throw new RuntimeException("The fake music provider cannot provide a path!");
    }

    @Override
    public List<Path> listAllMusicFiles() {
        return List.of();
    }
}
