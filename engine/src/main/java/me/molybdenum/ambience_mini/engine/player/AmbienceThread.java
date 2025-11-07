package me.molybdenum.ambience_mini.engine.player;

import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.player.music_picker.rules.Rule;
import javazoom.jlayer.decoder.JavaLayerException;
import me.molybdenum.ambience_mini.engine.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.state.monitors.VolumeMonitor;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AmbienceThread extends Thread
{
    private final Logger _logger;
    private final Supplier<Boolean> _isFocused;


    private final boolean _lostFocusEnabled;
    private final long _updateIntervalMilliseconds;
    private final long _nextMusicDelayMilliseconds;


    private boolean _kill = false;


    private final Random _rand = new Random(System.nanoTime());
    private final Rule _rule; // An object representation of the config file which decides the music.

    private MusicPlayer _mainPlayer = null;
    private MusicPlayer _interruptPlayer = null;

    private boolean _isHalted = false;
    private long _chooseNextMusicTime = 0L;


    private boolean _volumeZero = false;
    private final Consumer<Float> _volumeChangedHandler = volume -> {
        handleVolumeZero(volume);

        if (_mainPlayer != null)
            _mainPlayer.setVolume(volume);

        if (_interruptPlayer != null)
            _interruptPlayer.setVolume(volume);
    };


    public AmbienceThread(
        Rule rule,
        Logger logger,
        Supplier<Boolean> isFocused,
        BaseClientConfig config
    ) {
        _rule = rule;
        _logger = logger;
        _isFocused = isFocused;

        _lostFocusEnabled = config.lostFocusEnabled.get();
        _updateIntervalMilliseconds = config.updateInterval.get();
        _nextMusicDelayMilliseconds = config.nextMusicDelay.get();

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
            VolumeMonitor.registerVolumeHandler(_volumeChangedHandler);
            handleVolumeZero(VolumeMonitor.getVolume());

            long nextUpdate = System.currentTimeMillis();
            while (!_kill)
            {
                // Update at most every "UPDATE_INTERVAL_MILLISECONDS".
                TimeUnit.MILLISECONDS.sleep(nextUpdate - System.currentTimeMillis());
                nextUpdate = System.currentTimeMillis() + _updateIntervalMilliseconds;

                if (_volumeZero || handleUnfocused())
                    continue;

                handleMusicCycle();
            }
        }
        catch (Exception ex) {
            _logger.error("Error in AmbienceThread.run()", ex);
            stopMainMusic(false);
            stopInterruptMusic(false);
        }
        finally {
            VolumeMonitor.unregisterVolumeHandler(_volumeChangedHandler);
        }
    }

    public void kill()
    {
        if (isAlive()) {
            _kill = true;
            stopMainMusic(false);
            stopInterruptMusic(false);
            VolumeMonitor.unregisterVolumeHandler(_volumeChangedHandler);

            try {
                interrupt();
            } catch (Throwable ex) {
                _logger.error("Error in MusicPlayerThread.kill()", ex);
            }
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
        if (_lostFocusEnabled) {
            boolean isUnfocused = !_isFocused.get();
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

            if (musicStillValid && resumeMusic())
                return;
            else {
                stopInterruptMusic(false);
                stopMainMusic(false);
            }
        }

        if (nextIsInterrupt && _mainPlayer != null && _mainPlayer.isPlaying())
            _mainPlayer.pause(doFade);
        else if (!nextIsInterrupt && _interruptPlayer != null) {
            stopInterruptMusic(true);
            if (!musicStillValid)
                stopMainMusic(false);
        }

        else if (nextPlaylist.isEmpty())
            if (nextIsInterrupt) stopInterruptMusic(true);
            else stopMainMusic(true);

        else if (!musicStillValid || System.currentTimeMillis() > _chooseNextMusicTime) {
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

        Music nextMusic = playlist.get(getRandom(playlist.size()));
        while (nextMusic == currentMusic)
            nextMusic = playlist.get(getRandom(playlist.size()));

        return nextMusic;
    }


    private MusicPlayer playMusic(Music nextMusic, boolean fade) {
        MusicPlayer musicPlayer = new MusicPlayer(
            nextMusic,
            VolumeMonitor.getVolume(),
            () -> _chooseNextMusicTime = System.currentTimeMillis() + _nextMusicDelayMilliseconds,
            _logger
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

    private boolean resumeMusic()
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


    // ----------------------------------------------------------------------------------------------------------------
    // Utilities

    // The most random number generator I could think of.
    // "_rand.nextInt" alone just didn't... feel random...;
    private int getRandom(int max) {
        int iterations = _rand.nextInt(10,50);
        int acc = 0;
        while (iterations > 0) {
            acc += _rand.nextInt(0, 10000);
            iterations--;
        }
        return (int)((acc + System.nanoTime()) % max);
    }

    // Allowing for other parts of the program to force a new soundtrack to be selected
    public void forceSelectNewMusic() {
        _chooseNextMusicTime = 0L;
    }
}
