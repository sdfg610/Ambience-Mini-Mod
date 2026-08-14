package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class FakeMusicProvider extends BaseMusicProvider
{
    public FakeMusicProvider() {
        super("");
    }


    @Override
    public boolean existsLocally(String musicPath) {
        return true;
    }

    @Override
    public List<Path> listLocalMusicFiles() {
        return List.of();
    }

    @Override
    public int getLocalMusicSize(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get a music size!");
    }

    @Override
    public InputStream getLocalMusicStream(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get a music stream!");
    }

    @Override
    public int getServerMusicSize(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get a music size!");
    }

    @Override
    public InputStream getServerMusicStream(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get a music stream!");
    }
}
