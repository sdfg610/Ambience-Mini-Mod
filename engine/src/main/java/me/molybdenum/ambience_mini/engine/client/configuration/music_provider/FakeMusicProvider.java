package me.molybdenum.ambience_mini.engine.client.configuration.music_provider;

import java.io.BufferedInputStream;

public class FakeMusicProvider implements MusicProvider {
    @Override
    public boolean exists(String musicPath) {
        return true;
    }

    @Override
    public BufferedInputStream getMusicStream(String musicPath) {
        throw new RuntimeException("The fake music provider cannot get music streams!");
    }
}
