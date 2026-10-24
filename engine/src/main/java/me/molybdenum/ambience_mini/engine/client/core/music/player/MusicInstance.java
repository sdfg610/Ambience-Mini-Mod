package me.molybdenum.ambience_mini.engine.client.core.music.player;

import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfoCache;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.shared.core.music.music_provider.BaseMusicProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

public class MusicInstance {
    public final Music music;
    private final BaseMusicProvider provider;
    private final MusicInfoCache musicInfoCache;


    public MusicInstance(Music music, BaseMusicProvider provider, MusicInfoCache musicInfoCache) {
        this.music = music;
        this.provider = provider;
        this.musicInfoCache = musicInfoCache;
    }


    public Path getLocalPath() {
        return provider.getLocalPath(music.path());
    }


    public InputStream createStream() throws FileNotFoundException {
        return provider.getMusicStream(music);
    }

    public AmDecoder createDecoder() {
        return AmDecoder.decoderOf(this);
    }

    public int getMusicSize() throws FileNotFoundException {
        return provider.getMusicSize(music);
    }


    public MusicInfo getMusicInfoBlocking() {
        return music.locatedOnServer()
                ? musicInfoCache.getServerMusicInfoBlocking(music.path())
                : musicInfoCache.getLocalMusicInfoBlocking(this);
    }

    public MusicInfo getMusicInfo() {
        return music.locatedOnServer()
                ? musicInfoCache.getServerMusicInfo(music.path())
                : musicInfoCache.getLocalMusicInfo(this);
    }

    public void getMusicInfo(Consumer<MusicInfo> callback) {
        if (music.locatedOnServer())
            musicInfoCache.getServerMusicInfo(music.path(), callback);
        else
            musicInfoCache.getLocalMusicInfo(this, callback);
    }
}
