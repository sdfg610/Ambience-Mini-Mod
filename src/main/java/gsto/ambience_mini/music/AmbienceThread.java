package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.music.player.rule.PlaylistChoice;
import gsto.ambience_mini.music.player.rule.Rule;
import gsto.ambience_mini.music.state.Event;
import gsto.ambience_mini.music.player.Music;
import gsto.ambience_mini.music.player.MusicPlayer;
import gsto.ambience_mini.music.player.VolumeMonitor;
import gsto.ambience_mini.music.state.GameStateMonitor;
import gsto.ambience_mini.setup.Config;
import javazoom.jlayer.decoder.JavaLayerException;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AmbienceThread extends Thread
{
    private static final long UPDATE_INTERVAL_MILLISECONDS = 100;
    private static final long NEXT_MUSIC_DELAY_MILLISECONDS = 4000;


    private boolean _kill = false;


    private final Random _rand = new Random(System.currentTimeMillis());
    private final Rule _rule; // An object representation of the config file which decides the music.

    private MusicPlayer _mainPlayer = null;
    private MusicPlayer _interruptPlayer = null;

    private boolean _isHalted = false;
    private long _chooseNextMusicTime = 0L;


    private boolean _volumeZero = false;
    private final Consumer<Float> volumeChangedHandler = volume -> {
        handleVolumeZero(volume);

        if (_mainPlayer != null)
            _mainPlayer.setVolume(volume);

        if (_interruptPlayer != null)
            _interruptPlayer.setVolume(volume);
    };


    public AmbienceThread(Rule rule) {
        _rule = rule;

        setDaemon(true);
        setName("Ambience Mini - Music Monitor Thread");
        start();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Thread control
    @Override
    public void run()
    {
        try {
            VolumeMonitor.registerVolumeHandler(volumeChangedHandler);
            handleVolumeZero(VolumeMonitor.getVolume());

            long nextUpdate = System.currentTimeMillis();
            while (!_kill)
            {
                // Update at most every "UPDATE_INTERVAL_MILLISECONDS".
                TimeUnit.MILLISECONDS.sleep(nextUpdate - System.currentTimeMillis());
                nextUpdate = System.currentTimeMillis() + UPDATE_INTERVAL_MILLISECONDS;

                if (_volumeZero || handleUnfocused())
                    continue;

                handleMusicCycle();
            }
        }
        catch (Exception ex) {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.run()", ex);
            stopMainMusic(false);
            stopInterruptMusic(false);
        }
        finally {
            VolumeMonitor.unregisterVolumeHandler(volumeChangedHandler);
        }
    }

    public void kill()
    {
        _kill = true;
        stopMainMusic(false);
        stopInterruptMusic(false);
        VolumeMonitor.unregisterVolumeHandler(volumeChangedHandler);

        try { interrupt(); }
        catch(Throwable ex) {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.kill()", ex);
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Player control
    private void handleVolumeZero(float volume) {
        _volumeZero = volume < .01f;
        if (_volumeZero) {
            stopMainMusic(false);
            stopInterruptMusic(false);
        }
    }

    private boolean handleUnfocused() {
        if (Config.lostFocusEnabled.get()) {
            boolean isUnfocused = !GameStateMonitor.isGameFocused();
            if (isUnfocused && !_isHalted)
                haltMusic();
            return isUnfocused;
        }
        return false;
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Music and volume
    private void handleMusicCycle() throws JavaLayerException
    {
        PlaylistChoice nextChoice = _rule.getNext();
        if (nextChoice == null)
            return;

        List<Music> nextPlaylist = nextChoice.playlist();
        boolean nextIsInterrupt = nextChoice.isInterrupt();
        boolean doFade = !nextChoice.isInstant();

        MusicPlayer activePlayer = nextIsInterrupt ? _interruptPlayer : _mainPlayer;
        Music currentMusic = activePlayer == null ? null : activePlayer.currentMusic;

        boolean musicStillValid = nextPlaylist.stream().anyMatch(music -> music.equals(currentMusic));

        if (_isHalted) {
            _isHalted = false;

            if (!nextIsInterrupt)
                stopInterruptMusic(false);

            if (musicStillValid && unHaltMusic())
                return;
            else {
                stopInterruptMusic(false);
                stopMainMusic(false);
            }
        }

        if (nextIsInterrupt && _mainPlayer != null && _mainPlayer.isPlaying()) // Check for playing since just pause
            _mainPlayer.pause(doFade);
        else if (!nextIsInterrupt && _interruptPlayer != null) {
            stopInterruptMusic(true);
            if (!musicStillValid)
                stopMainMusic(false);
        }

        else if (nextPlaylist.isEmpty())
            if (nextIsInterrupt) stopInterruptMusic(true);
            else stopMainMusic(true);

        else if (!musicStillValid || activePlayer == null || System.currentTimeMillis() > _chooseNextMusicTime) {
            Music nextMusic = selectMusic(nextPlaylist, currentMusic);
            if (nextIsInterrupt) {
                stopInterruptMusic(doFade);
                _interruptPlayer = playMusic(nextMusic, doFade);
            } else {
                stopMainMusic(doFade);
                _mainPlayer = playMusic(nextMusic, doFade);
            }
        }

        else if (!activePlayer.isPlaying())
            activePlayer.playOrResume(doFade);
    }


    private Music selectMusic(List<Music> playlist, Music currentMusic) {
        if (playlist.isEmpty())
            throw new RuntimeException("Cannot select music from empty playlist!");
        if (playlist.size() == 1)
            return playlist.get(0);

        Music nextMusic = playlist.get(_rand.nextInt(playlist.size()));
        while (nextMusic == currentMusic)
            nextMusic = playlist.get(_rand.nextInt(playlist.size()));

        return playlist.get(_rand.nextInt(playlist.size()));
    }


    private MusicPlayer playMusic(Music nextMusic, boolean fade) throws JavaLayerException {
        MusicPlayer musicPlayer = new MusicPlayer(
                nextMusic,
                VolumeMonitor.getVolume(),
                () -> _chooseNextMusicTime = System.currentTimeMillis() + NEXT_MUSIC_DELAY_MILLISECONDS
        );
        musicPlayer.playOrResume(fade);
        _chooseNextMusicTime = Long.MAX_VALUE;

        return musicPlayer;
    }

    private void haltMusic()
    {
        _isHalted = true;
        if(_interruptPlayer != null)
            _interruptPlayer.pause(true);
        if(_mainPlayer != null)
            _mainPlayer.pause(true);
    }

    private boolean unHaltMusic()
    {
        if (_interruptPlayer != null)
            _interruptPlayer.playOrResume(true);
        else if (_mainPlayer != null)
            _mainPlayer.playOrResume(true);
        else
            return false;
        return true;
    }


    private void stopMainMusic(boolean fadeOut) {
        if (_mainPlayer != null) {
            try { _mainPlayer.stop(fadeOut); }
            catch (Exception ignored) { }
            _mainPlayer = null;
        }
    }

    private void stopInterruptMusic(boolean fadeOut) {
        if (_interruptPlayer != null) {
            try { _interruptPlayer.stop(fadeOut); }
            catch (Exception ignored) { }
            _interruptPlayer = null;
        }
    }
}
