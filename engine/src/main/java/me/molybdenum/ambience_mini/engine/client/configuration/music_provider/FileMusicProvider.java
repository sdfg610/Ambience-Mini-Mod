package me.molybdenum.ambience_mini.engine.client.configuration.music_provider;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    public InputStream getMusicStream(String musicPath) throws FileNotFoundException {
        return new FileInputStream(getFullPath(musicPath).toFile());
    }

    @Override
    public int getMusicSize(String musicPath) throws FileNotFoundException {
        var file = getFullPath(musicPath).toFile();
        if (file.exists()) {
            if (file.isFile())
                return Math.toIntExact(file.length());
            else
                throw new RuntimeException("The path '" + musicPath + "' does not denote a file.");
        }
        else
            throw new FileNotFoundException("No file found at path '" + musicPath + "'");
    }

    @Override
    public Path getFullPath(String musicPath) {
        return Path.of(musicBasePath, musicPath);
    }

    @Override
    public List<Path> listAllMusicFiles() {
        try (var files = Files.walk(Path.of(musicBasePath))) {
            return files.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
