package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import javazoom.jl.decoder.JavaLayerException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.TimeUnit;

public class MusicManagerThread extends Thread
{
    private static final long UPDATE_INTERVAL_MILLISECONDS = 100;
    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;


    private MusicPlayer _player = null;
    private boolean _kill = false;


    public MusicManagerThread() {
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
            long nextUpdate = System.currentTimeMillis();
            while (!_kill)
            {
                TimeUnit.MILLISECONDS.sleep(nextUpdate - System.currentTimeMillis());
                nextUpdate = System.currentTimeMillis() + UPDATE_INTERVAL_MILLISECONDS;

                Minecraft mc = Minecraft.getInstance();
                LocalPlayer mcPlayer = mc.player;

                if (_player != null && GameStateManager.possiblyInSoundOptions(mc))
                    _player.setGain(getRealGain());

                if (GameStateManager.inMainMenu(mc)) {
                    if (GameStateManager.isJoiningWorld())
                        playMusic(MusicLoader.MUSIC_JOINING, true, true);
                    else
                        playMusic(MusicLoader.MUSIC_MAIN_MENU, true, true);
                }
                else if (mcPlayer != null && GameStateManager.inGame(mc))
                {
                    if (GameStateManager.isDead(mcPlayer))
                        playMusic(MusicLoader.MUSIC_DEAD, false, false);
                    else
                        playMusic(MusicLoader.MUSIC_CHILL_DAY1, true, true);
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
            _kill = true;
            stopMusic(false);
            interrupt();
        } catch(Throwable ex) {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.kill()", ex);
        }
    }


    //
    // Music and volume
    //

    public void playMusic(Music nextMusic, boolean fadeInNext, boolean fadeOutCurrent) throws JavaLayerException, InterruptedException
    {
        if (_player == null || _player.currentMusic != nextMusic)
        {
            if (_player != null)
                stopMusic(fadeOutCurrent);
            _player = new MusicPlayer(nextMusic, fadeInNext ? MusicPlayer.MIN_GAIN : getRealGain());
            _player.startMusicThread();

            if (fadeInNext)
                fadeIn();
        }
    }

    public void stopMusic(boolean fadeOut) throws InterruptedException
    {
        if(_player != null)
        {
            if (fadeOut)
                fadeOut();
            _player.stopMusicThreadAndCloseStreams();
            _player = null;
        }
    }

    public float getRealGain()
    {
        Options settings = Minecraft.getInstance().options;
        float musicGain = settings.getSoundSourceVolume(SoundSource.MUSIC) * settings.getSoundSourceVolume(SoundSource.MASTER);
        return (MusicPlayer.MIN_GAIN + (MusicPlayer.MAX_GAIN - MusicPlayer.MIN_GAIN) * musicGain);
    }

    private void fadeIn() throws InterruptedException
    {
        float real = getRealGain();
        float diff = Math.abs(real - MusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        for (int i = FADE_STEP_COUNT - 1; i >= 0; i--) {
            _player.setGain(real - diff*i);
            TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
        }
        _player.setGain(real);
    }


    private void fadeOut() throws InterruptedException
    {
        float real = getRealGain();
        float diff = Math.abs(real - MusicPlayer.MIN_GAIN) / FADE_STEP_COUNT;
        for (int i = 0; i < FADE_STEP_COUNT; i++) {
            _player.setGain(real - diff*i);
            TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
        }
        _player.setGain(MusicPlayer.MIN_GAIN);
    }
}
