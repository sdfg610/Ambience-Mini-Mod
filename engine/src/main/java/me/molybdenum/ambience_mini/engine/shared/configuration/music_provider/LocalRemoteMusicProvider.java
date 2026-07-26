package me.molybdenum.ambience_mini.engine.shared.configuration.music_provider;

import me.molybdenum.ambience_mini.engine.client.core.server_music.ServerMusicCache;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.streams.FullyBufferedInputStream;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class LocalRemoteMusicProvider extends LocalMusicProvider
{
    private final ServerMusicCache serverMusicCache;


    public LocalRemoteMusicProvider(String musicBasePath, ServerMusicCache serverMusicCache) {
        super(musicBasePath);

        this.serverMusicCache = serverMusicCache;
    }


    @Override
    public int getMusicSize(Music music) throws FileNotFoundException {
        return music.locatedOnServer()
                ? serverMusicCache.getMusicSize(music.path())
                : super.getMusicSize(music);
    }

    @Override
    public InputStream getMusicStream(Music music) throws FileNotFoundException {
        return music.locatedOnServer()
                ? new FullyBufferedInputStream(serverMusicCache.getMusicStream(music.path()), false)
                : super.getMusicStream(music);
    }
}
