package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

public class MusicPlayer
{
    public static final float MIN_GAIN = -50F;
    public static final float MAX_GAIN = 0F;


    private boolean _isPlaying = false;
    private final InputStream _musicStream;
    private final AdvancedPlayer _player;
    private final Thread _playerThread;

    public final Music currentMusic;


    public MusicPlayer(Music music, float gain) throws JavaLayerException {
        this(music, gain, null);
    }

    public MusicPlayer(Music music, float gain, @Nullable Runnable onDonePlaying) throws JavaLayerException {
        currentMusic = music;
        _musicStream = currentMusic.getMusicStream();
        _player = new AdvancedPlayer(_musicStream, gain);

        _playerThread = new Thread(() -> {
            try {
                _player.play();
                _isPlaying = false;
                if (onDonePlaying != null)
                    onDonePlaying.run();
            } catch (Exception ex) {
                AmbienceMini.LOGGER.error("Error in MusicPlayer's internal thread", ex);
            }
        });
        _playerThread.setDaemon(true);
        _playerThread.setName("Ambience Mini - Music Player Thread");
    }


    // Controls

    public void startMusicThread()
    {
        _isPlaying = true;
        _playerThread.start();
    }

    public void stopMusicThreadAndCloseStreams()
    {
        _player.close();
        try {
            _musicStream.close();
        } catch (IOException ignore) { }
    }

    public boolean isPlaying()
    {
        return _isPlaying;
    }


    // Volume

    public void setGain(float gain)
    {
        _player.setGain(gain);
    }
}
