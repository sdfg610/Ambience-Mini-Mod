package me.molybdenum.ambience_mini.engine.shared.music.music_provider;

import me.molybdenum.ambience_mini.engine.client.core.music.ServerMusicCache;
import me.molybdenum.ambience_mini.engine.shared.music.streams.FullyBufferedInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RealMusicProvider extends BaseMusicProvider
{
    @Nullable
    private final ServerMusicCache musicCache;


    public RealMusicProvider(String musicBasePath, @Nullable ServerMusicCache musicCache) {
        super(musicBasePath);
        this.musicCache = musicCache;
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
    public int getLocalMusicSize(String musicPath) throws FileNotFoundException {
        assertPathValid(musicPath);
        var localPath = getLocalPath(musicPath);
        var file = localPath.toFile();
        if (file.exists()) {
            if (file.isFile())
                return Math.toIntExact(file.length());
            else
                throw new FileNotFoundException("The path '" + localPath + "' does not denote a file.");
        }
        else
            throw new FileNotFoundException("No file found at path '" + localPath + "'");
    }

    @Override
    public InputStream getLocalMusicStream(String musicPath) throws FileNotFoundException {
        assertPathValid(musicPath);
        return new FileInputStream(getLocalPath(musicPath).toFile());
    }


    @Override
    public int getServerMusicSize(String musicPath) throws FileNotFoundException {
        assertPathValid(musicPath);
        if (musicCache == null)
            throw new FileNotFoundException("This music provider can only provide local music sizes.");
        return musicCache.getMusicSize(musicPath);
    }

    @Override
    public InputStream getServerMusicStream(String musicPath) throws FileNotFoundException {
        assertPathValid(musicPath);
        if (musicCache == null)
            throw new FileNotFoundException("This music provider can only provide local music streams.");
        return new FullyBufferedInputStream(musicCache.getMusicBuffer(musicPath), false);
    }


    private void assertPathValid(String musicPath) {
        var res = validatePath(musicPath);
        if (res.isFailure())
            throw new RuntimeException("Error while fetching music: " + res.error);
    }
}
