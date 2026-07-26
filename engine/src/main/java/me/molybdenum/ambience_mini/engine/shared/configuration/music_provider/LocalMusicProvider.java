package me.molybdenum.ambience_mini.engine.shared.configuration.music_provider;

import me.molybdenum.ambience_mini.engine.shared.music.Music;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocalMusicProvider extends BaseMusicProvider
{
    public LocalMusicProvider(String musicBasePath) {
        super(musicBasePath);
    }


    @Override
    public boolean existsLocally(String musicPath) {
        return Files.exists(Path.of(basePath, musicPath));
    }

    @Override
    public List<Path> listLocalMusicFiles() {
        try (var files = Files.walk(Path.of(basePath))) {
            return files.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int getMusicSize(Music music) throws FileNotFoundException {
        if (music.locatedOnServer())
            throw new RuntimeException("This music provider cannot provide information on server music");

        var localPath = getLocalPath(music);
        var file = localPath.toFile();
        if (file.exists()) {
            if (file.isFile())
                return Math.toIntExact(file.length());
            else
                throw new RuntimeException("The path '" + localPath + "' does not denote a file.");
        }
        else
            throw new FileNotFoundException("No file found at path '" + localPath + "'");
    }

    @Override
    public InputStream getMusicStream(Music music) throws FileNotFoundException {
        if (music.locatedOnServer())
            throw new RuntimeException("This music provider cannot provide information on server music");

        return new FileInputStream(getLocalPath(music).toFile());
    }
}
