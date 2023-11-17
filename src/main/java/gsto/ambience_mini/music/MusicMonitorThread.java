package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.setup.Config;
import gsto.ambience_mini.state.MenuStateChecker;
import javazoom.jl.decoder.JavaLayerException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.TimeUnit;

public class MusicMonitorThread extends Thread
{
    public static final float MIN_GAIN = -50F;
    public static final float MAX_GAIN = 0F;
    public static final float[] fadeGains;

    static {
        fadeGains = new float[Config.fadeDuration.get()]; // Change to first run when necessary
        float total_diff = MIN_GAIN - MAX_GAIN;
        float diff = total_diff / fadeGains.length;
        for(int i = 0; i < fadeGains.length; i++)
            fadeGains[i] = MAX_GAIN + diff * i;
    }

    private boolean _kill = false;

    private volatile float _gain = MAX_GAIN;
    private MusicPlayer _player = null;


    public MusicMonitorThread() {
        setDaemon(true);
        setName("Ambience Mini - Music Monitor Thread");
        start();
    }


    //
    // Thread control
    //

    @Override
    public void run()
    {
        try {
            while (!_kill)
            {
                TimeUnit.MILLISECONDS.sleep(250);
                Minecraft mc = Minecraft.getInstance();

                if (MenuStateChecker.inMainMenu(mc))
                {
                    playMusicInstant(MusicLoader.MUSIC_JOINING);
                }
            }
        }
        catch (Exception ex)
        {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.run()", ex);
        }
    }

    public void kill()
    {
        try {
            stopMusic();
            _kill = true;
            interrupt();
        } catch(Throwable ex) {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.kill()", ex);
        }
    }


    //
    // Music and volume
    //

    public void transitionMusic(Music nextMusic)
    {
        if(_player != null)
            _player.stopMusicThreadAndCloseStreams();
        _player = null;
    }

    public void playMusicInstant(Music nextMusic) throws JavaLayerException {
        if (_player == null || _player.currentMusic != nextMusic)
        {
            if (_player != null)
                _player.stopMusicThreadAndCloseStreams();
            _player = new MusicPlayer(nextMusic);
            _player.startMusicThread();
        }
    }

    public void stopMusic()
    {
        if(_player != null)
            _player.stopMusicThreadAndCloseStreams();
        _player = null;
    }

    public float getRealGain()
    {
        Options settings = Minecraft.getInstance().options;
        float musicGain = settings.getSoundSourceVolume(SoundSource.MUSIC) * settings.getSoundSourceVolume(SoundSource.MASTER);
        return (MIN_GAIN + (_gain - MIN_GAIN) * musicGain);
    }

    public void setGain(float gain) {
        this._gain = Math.min(MAX_GAIN, Math.max(MIN_GAIN, gain));
        if(_player != null)
            _player.setGain(getRealGain());
    }
}
