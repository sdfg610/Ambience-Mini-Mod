package me.molybdenum.ambience_mini.engine.player;

import javazoom.jlayer.decoder.JavaLayerException;
import javazoom.jlayer.player.controller.MP3Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NewMusicPlayer
{
    public static final float MIN_GAIN = -50F;
    public static final float DEFAULT_MAX_GAIN = 0F;

    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;

    private boolean _suppressOnPlayedToEnd;

    private final AtomicBoolean _isPlaying = new AtomicBoolean(false);
    private InputStream _musicStream = null;
    private MP3Player _player = null;


    public final Music currentMusic;
    private float _currentGain;


    public NewMusicPlayer(Music music, float volume, @Nullable Runnable onPlayedToEnd, Logger logger) throws JavaLayerException {
        currentMusic = music;
        try {
            _musicStream = currentMusic.getMusicStream();
            _player = new MP3Player(_musicStream, _currentGain);
            setVolume(volume);

            getThread(onPlayedToEnd, logger).start();
        } catch (FileNotFoundException ex) {
            logger.error("File '{}' not found. Fix your Ambience config!", currentMusic.musicName, ex);
        }
    }

    private @NotNull Thread getThread(@Nullable Runnable onPlayedToEnd, Logger logger) {
        Thread _playerThread = new Thread(() -> {
            try {
                _player.play(_isPlaying);
                _isPlaying.set(false);

                try { _musicStream.close(); }
                catch (IOException ignore) { }

                if (!_suppressOnPlayedToEnd && onPlayedToEnd != null)
                    onPlayedToEnd.run();
            } catch (Throwable ex) {
                logger.error("Error in MusicPlayer's internal thread", ex);
            }
        });
        _playerThread.setDaemon(true);
        _playerThread.setName("Ambience Mini - Music Player Thread");
        return _playerThread;
    }


    // Controls
    public void playOrResume(boolean fadeIn) {
        if (_isPlaying.get())
            return;

        if (fadeIn) {
            _player.setGain(MIN_GAIN);
            _isPlaying.set(true);
            fadeIn();
        } else {
            _player.setGain(_currentGain);
            _isPlaying.set(true);
        }
    }

    public void pause(boolean fadeOut) {
        if (!_isPlaying.get())
            return;

        if (fadeOut)
            fadeOut();

        _isPlaying.set(false);
    }

    public void stop(boolean fadeOut) {
        _suppressOnPlayedToEnd = true;

        if (fadeOut)
            fadeOut();

        _isPlaying.set(false);
        _player.close();
    }

    public boolean isPlaying() {
        return _isPlaying.get();
    }


    // Volume
    public void setVolume(float volume) {
        _currentGain = MIN_GAIN + ((DEFAULT_MAX_GAIN + currentMusic.gain) - MIN_GAIN) * volume;
        _player.setGain(_currentGain);
    }

    private void fadeIn() {
        float diff = Math.abs(_currentGain - NewMusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        try {
            for (int i = FADE_STEP_COUNT - 1; i >= 0; i--) {
                _player.setGain(_currentGain - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }
        } catch (Throwable ignored) { }

        _player.setGain(_currentGain);
    }

    private void fadeOut() {
        float diff = Math.abs(_currentGain - NewMusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        try {
            for (int i = 0; i < FADE_STEP_COUNT; i++) {
                _player.setGain(_currentGain - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }
        } catch (Throwable ignored) { }

        _player.setGain(NewMusicPlayer.MIN_GAIN);
    }
}
