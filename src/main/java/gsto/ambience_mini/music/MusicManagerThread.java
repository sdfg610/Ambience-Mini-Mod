package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.setup.Config;
import javazoom.jl.decoder.JavaLayerException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MusicManagerThread extends Thread
{
    private static final long UPDATE_INTERVAL_MILLISECONDS = 100;
    private static final long NEXT_MUSIC_DELAY_MILLISECONDS = 2000;

    private static final long HIGH_UP_THRESHOLD = 150;


    private MusicPlayer _musicPlayer = null;
    private boolean _kill = false;

    private final Random _rand = new Random(System.currentTimeMillis());
    private String _currentEvent = null;
    private List<Music> _currentMusicChoices = List.of();

    private long _chooseNextMusicTime = Long.MAX_VALUE;


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
            boolean isPaused = false;
            long nextUpdate = System.currentTimeMillis();
            while (!_kill)
            {
                // Update at most every "UPDATE_INTERVAL_MILLISECONDS".
                TimeUnit.MILLISECONDS.sleep(nextUpdate - System.currentTimeMillis());
                nextUpdate = System.currentTimeMillis() + UPDATE_INTERVAL_MILLISECONDS;

                if (Config.lostFocusEnabled.get() && _musicPlayer != null) {
                    if (!GameStateManager.isGameFocused()) {
                        pauseMusic();
                        isPaused = true;
                        continue;
                    }
                    else if (isPaused) {
                        _musicPlayer.playOrResume(true);
                        isPaused = false;
                    }
                }

                // Don't waste resources setting gain if in a state where you cannot possibly be in the sound menu.
                if (_musicPlayer != null && GameStateManager.possiblyInSoundOptions())
                    _musicPlayer.setGain(getRealGain());

                if (GameStateManager.inMainMenu()) {
                    boolean fireAndForget =
                            (GameStateManager.isJoiningWorld() && setCurrentEvent(MusicEvents.CONNECTING))
                            || (GameStateManager.onDisconnectedScreen() && setCurrentEvent(MusicEvents.DISCONNECTED))
                            || setCurrentEvent(MusicEvents.MAIN_MENU);
                }
                else if (GameStateManager.inGame())
                {
                    boolean fireAndForget =
                            (GameStateManager.isDead() && setCurrentEvent(MusicEvents.DEAD))
                            || (GameStateManager.onCreditsScreen() && setCurrentEvent(MusicEvents.CREDITS))
                            || setBossBasedMusicEvent(GameStateManager.getBossId())
                            || (GameStateManager.isSleeping() && setCurrentEvent(MusicEvents.SLEEPING))
                            || (GameStateManager.isFishing() && setCurrentEvent(MusicEvents.FISHING))
                            || setAreaBasedMusicEvent()
                            || setElevationBasedMusicEvent();
                }

                handleMusicCycle();
            }
        }
        catch (Exception ex)
        {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.run()", ex);
            try {
                stopMusic(false);
            } catch (InterruptedException ignored) { }
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
    // Event control
    //

    private boolean setAreaBasedMusicEvent()
    {
        return (GameStateManager.isInVillage() && setCurrentEvent(MusicEvents.VILLAGE));
    }

    private boolean setElevationBasedMusicEvent()
    {
        int playerElevation = GameStateManager.getPlayerElevation(); // The 'Y' coordinate.
        return
                (GameStateManager.isUnderWater() && setCurrentEvent(MusicEvents.UNDERWATER))
                || (playerElevation > HIGH_UP_THRESHOLD && setCurrentEvent(MusicEvents.HIGH_UP))
                || (playerElevation < 0 && setCurrentEvent(MusicEvents.UNDER_DEEPSLATE))
                || (GameStateManager.isUnderground() && setCurrentEvent(MusicEvents.UNDERGROUND))
                || setCurrentEvent(MusicEvents.DEFAULT);
    }

    private boolean setBossBasedMusicEvent(String bossName)
    {
        if (bossName == null)
            return false;

        var bossEvent = "boss:" + bossName;
        var newMusic = MusicRegistry.getBossMusic(bossName);
        if (newMusic != _currentMusicChoices && newMusic != null) {
            _currentEvent = bossEvent;
            _currentMusicChoices = newMusic;
            return true;
        }

        return _currentEvent.equals(bossEvent);
    }

    private boolean setCurrentEvent(String event)
    {
        var newMusic = MusicRegistry.getEventMusic(event);
        if (newMusic != _currentMusicChoices && newMusic != null) {
            _currentEvent = event;
            _currentMusicChoices = newMusic;
            return true;
        }

        return _currentEvent.equals(event);
    }


    //
    // Music and volume
    //

    private void handleMusicCycle() throws JavaLayerException, InterruptedException
    {
        boolean fade = !_currentEvent.equals(MusicEvents.DEAD) || _musicPlayer == null;

        Music currentMusic = null;
        if (_musicPlayer != null)
            currentMusic = _musicPlayer.currentMusic;

        if (!_currentMusicChoices.isEmpty())
        {
            int currentMusicIndex = _currentMusicChoices.indexOf(currentMusic);
            if (currentMusicIndex == -1)
                playMusic(_currentMusicChoices.get(_rand.nextInt(_currentMusicChoices.size())), fade, fade);
            else if (System.currentTimeMillis() > _chooseNextMusicTime) {
                int nextMusicIndex = _rand.nextInt(_currentMusicChoices.size());
                while (nextMusicIndex == currentMusicIndex)
                    nextMusicIndex = _rand.nextInt(_currentMusicChoices.size());

                playMusic(_currentMusicChoices.get(nextMusicIndex), fade, fade);
            }
        }
        else if (_musicPlayer != null && _musicPlayer.isPlaying())
            _musicPlayer.stop(true, true);
    }

    private void playMusic(Music nextMusic, boolean fadeInNext, boolean fadeOutCurrent) throws JavaLayerException, InterruptedException
    {
        if (nextMusic != null && (_musicPlayer == null || _musicPlayer.currentMusic != nextMusic))
        {
            if (_musicPlayer != null)
                stopMusic(fadeOutCurrent);
            _musicPlayer = new MusicPlayer(
                    nextMusic,
                    getRealGain(),
                    () -> _chooseNextMusicTime = System.currentTimeMillis() + NEXT_MUSIC_DELAY_MILLISECONDS
            );
            _musicPlayer.playOrResume(fadeInNext);
        }
        _chooseNextMusicTime = Long.MAX_VALUE;
    }

    private void pauseMusic() throws InterruptedException
    {
        if(_musicPlayer != null)
            _musicPlayer.pause(true);
    }

    private void stopMusic(boolean fadeOut) throws InterruptedException
    {
        if(_musicPlayer != null)
        {
            _musicPlayer.stop(fadeOut, true);
            _musicPlayer = null;
        }
    }

    private float getRealGain()
    {
        Options settings = Minecraft.getInstance().options;
        float musicGain = settings.getSoundSourceVolume(SoundSource.MUSIC) * settings.getSoundSourceVolume(SoundSource.MASTER);
        return (MusicPlayer.MIN_GAIN + (MusicPlayer.MAX_GAIN - MusicPlayer.MIN_GAIN) * musicGain);
    }
}
