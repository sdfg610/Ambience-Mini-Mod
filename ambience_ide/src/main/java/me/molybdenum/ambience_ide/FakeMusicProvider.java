package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.shared.configuration.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.music.Music;

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
    public InputStream getMusicStream(Music music) {
        throw new RuntimeException("The fake music provider cannot get a music stream!");
    }

    @Override
    public int getMusicSize(Music music) {
        throw new RuntimeException("The fake music provider cannot get a music size!");
    }
}
