package me.molybdenum.ambience_mini.engine.music;

import me.molybdenum.ambience_mini.engine.configuration.Music;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.music.players.FlacPlayer;
import me.molybdenum.ambience_mini.engine.music.players.MP3Player;
import me.molybdenum.ambience_mini.engine.music.players.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MusicPlayer
{
    public static final List<String> SUPPORTED_FILE_TYPES = List.of("mp3", "flac");

    public static final float MIN_GAIN = -30F;
    public static final float DEFAULT_MAX_GAIN = 0F;

    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;

    public final Music music;
    private final MusicProvider musicProvider;
    private Player _player = null;
    private float _currentGain;


    public MusicPlayer(Music music, float volume, MusicProvider musicProvider, @Nullable Runnable onPlayedToEnd, Logger logger) {
        this.music = music;
        this.musicProvider = musicProvider;
        try {
            _player = createPlayer(music, onPlayedToEnd, logger);
            setVolume(volume);
            _player.play();
        } catch (FileNotFoundException ex) {
            logger.error("File '{}' not found. Fix your Ambience config!", music.musicPath(), ex);
        } catch (RuntimeException ex) {
            logger.error("A problem happened while trying to play '{}'!", music.musicPath(), ex);
        }
    }

    private Player createPlayer(Music music, @Nullable Runnable onPlayedToEnd, Logger logger) throws FileNotFoundException {
        InputStream stream = musicProvider.getMusicStream(music.musicPath());

        Consumer<Boolean> onFinished = wasStopped -> {
            try { stream.close(); }
            catch (IOException ignore) { }
            if (!wasStopped && onPlayedToEnd != null)
                onPlayedToEnd.run();
        };

        if (music.isMP3())
            return new MP3Player(
                    stream,
                    ex -> logger.error("Error in MP3 player thread", ex),
                    onFinished
            );
        else if (music.isFLAC())
            return new FlacPlayer(
                    stream,
                    music.musicPath(),
                    ex -> logger.error("Error in FLAC player thread", ex),
                    onFinished
            );

        throw new RuntimeException("Unsupported file format! How did this get through the semantic analysis? Or is there another bug?");
    }


    // ------------------------------------------------------------------------------------------
    // Controls
    public void playOrResume(boolean fadeIn) {
        if (!_player.isPlaying()) {
            if (fadeIn) {
                _player.setGain(MIN_GAIN);
                _player.play();
                fadeIn();
            } else {
                _player.setGain(_currentGain);
                _player.play();
            }
        }
    }

    public void pause(boolean fadeOut) {
        if (_player.isPlaying()) {
            if (fadeOut)
                fadeOut();
            _player.pause();
        }
    }

    public void stop(boolean fadeOut) {
        if (fadeOut)
            fadeOut();

        _player.stop();
    }

    public boolean isPlaying() {
        return _player.isPlaying();
    }


    // Volume
    public void setVolume(float volume) {
        _currentGain = MIN_GAIN + ((DEFAULT_MAX_GAIN + music.gain()) - MIN_GAIN) * volume;
        _player.setGain(_currentGain);
    }

    private void fadeIn() {
        float diff = Math.abs(_currentGain - MusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        try {
            for (int i = FADE_STEP_COUNT - 1; i >= 0; i--) {
                _player.setGain(_currentGain - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }
        } catch (Throwable ignored) { }

        _player.setGain(_currentGain);
    }

    private void fadeOut() {
        float diff = Math.abs(_currentGain - MusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        try {
            for (int i = 0; i < FADE_STEP_COUNT; i++) {
                _player.setGain(_currentGain - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }
        } catch (Throwable ignored) { }

        _player.setGain(MusicPlayer.MIN_GAIN);
    }
}
