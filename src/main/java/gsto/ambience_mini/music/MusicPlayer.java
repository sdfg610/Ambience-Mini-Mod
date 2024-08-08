package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import javazoom.jlayer.decoder.JavaLayerException;
import javazoom.jlayer.player.advanced.AdvancedPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicPlayer
{
    public static final float MIN_GAIN = -50F;
    public static final float MAX_GAIN = 0F;

    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;


    private float _currentGain;
    private boolean _suppressOnDonePlaying;

    private final AtomicBoolean _isPlaying = new AtomicBoolean(false);
    private final InputStream _musicStream;
    private final AdvancedPlayer _player;


    public final Music currentMusic;


    public MusicPlayer(Music music, float gain) throws JavaLayerException {
        this(music, gain, null);
    }

    public MusicPlayer(Music music, float gain, @Nullable Runnable onDonePlaying) throws JavaLayerException {
        currentMusic = music;
        _currentGain = gain;
        _musicStream = currentMusic.getMusicStream();
        _player = new AdvancedPlayer(_musicStream, _currentGain);

        Thread _playerThread = new Thread(() -> {
            try {
                _player.play(_isPlaying);
                _isPlaying.set(false);
                if (!_suppressOnDonePlaying && onDonePlaying != null)
                    onDonePlaying.run();
            } catch (Exception ex) {
                AmbienceMini.LOGGER.error("Error in MusicPlayer's internal thread", ex);
            }
        });
        _playerThread.setDaemon(true);
        _playerThread.setName("Ambience Mini - Music Player Thread");
        _playerThread.start();
    }


    // Controls
    public void playOrResume(boolean fadeIn) throws InterruptedException {
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

    public void pause(boolean fadeOut) throws InterruptedException {
        if (!_isPlaying.get())
            return;

        if (fadeOut)
            fadeOut();

        _isPlaying.set(false);
    }

    public void stop(boolean fadeOut, boolean suppressOnDonePlaying) throws InterruptedException {
        _suppressOnDonePlaying = suppressOnDonePlaying;
        if (fadeOut)
            fadeOut();

        _player.close();
        try {
            _musicStream.close();
        } catch (IOException ignore) { }
    }

    public boolean isPlaying()
    {
        return _isPlaying.get();
    }


    // Volume

    public void setGain(float gain)
    {
        _currentGain = gain;
        _player.setGain(gain);
    }

    private void fadeIn() throws InterruptedException
    {
        float diff = Math.abs(_currentGain - MusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        for (int i = FADE_STEP_COUNT - 1; i >= 0; i--) {
            _player.setGain(_currentGain - diff*i);
            TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
        }
        _player.setGain(_currentGain);
    }


    private void fadeOut() throws InterruptedException
    {
        float diff = Math.abs(_currentGain - MusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        for (int i = 0; i < FADE_STEP_COUNT; i++) {
            _player.setGain(_currentGain - diff*i);
            TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
        }
        _player.setGain(MusicPlayer.MIN_GAIN);
    }
}
