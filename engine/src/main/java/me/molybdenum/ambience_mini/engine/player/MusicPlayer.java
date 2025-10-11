package me.molybdenum.ambience_mini.engine.player;

import javazoom.jlayer.player.MP3Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MusicPlayer
{
    public static final float MIN_GAIN = -50F;
    public static final float DEFAULT_MAX_GAIN = 0F;

    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;

    private boolean _suppressOnPlayedToEnd;

    public final Music currentMusic;
    private MP3Player _player = null;
    private float _currentGain;


    public MusicPlayer(Music music, float volume, @Nullable Runnable onPlayedToEnd, Logger logger) {
        currentMusic = music;
        try {
            InputStream stream = currentMusic.getMusicStream();
            _player = new MP3Player(
                    stream,
                    ex -> logger.error("Error in MP3 player thread", ex),
                    ignored -> {
                        try { stream.close(); }
                        catch (IOException ignore) { }

                        if (!_suppressOnPlayedToEnd && onPlayedToEnd != null)
                            onPlayedToEnd.run();
                    }
            );
            setVolume(volume);
            _player.play();
        } catch (FileNotFoundException ex) {
            logger.error("File '{}' not found. Fix your Ambience config!", currentMusic.musicName, ex);
        }
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
        _suppressOnPlayedToEnd = true;
        if (fadeOut)
            fadeOut();

        _player.stop();
    }

    public boolean isPlaying() {
        return _player.isPlaying();
    }


    // Volume
    public void setVolume(float volume) {
        _currentGain = MIN_GAIN + ((DEFAULT_MAX_GAIN + currentMusic.gain) - MIN_GAIN) * volume;
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
