package me.molybdenum.ambience_mini.engine.client.configuration.music_provider;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMusicProvider implements MusicProvider
{
    private final String musicBasePath;


    public FileMusicProvider(String musicBasePath) {
        this.musicBasePath = musicBasePath;
    }


    @Override
    public boolean exists(String musicPath) {
        return Files.exists(Path.of(musicBasePath, musicPath));
    }

    public BufferedInputStream getMusicStream(String musicPath) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(getFullPath(musicPath).toFile()));
    }

    @Override
    public Path getFullPath(String musicPath) {
        return Path.of(musicBasePath, musicPath);
    }
}
