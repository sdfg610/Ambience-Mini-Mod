package me.molybdenum.ambience_mini.engine.configuration.music_provider;

import java.io.InputStream;

public class FakeMusicProvider implements MusicProvider {
    @Override
    public boolean exists(String musicPath) {
        return true;
    }

    @Override
    public InputStream getMusicStream(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get music streams!");
    }
}
