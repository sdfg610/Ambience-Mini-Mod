package me.molybdenum.ambience_mini.engine.music;

import me.molybdenum.ambience_mini.engine.configuration.Music;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.core.BaseCore;
import me.molybdenum.ambience_mini.engine.core.state.VolumeState;
import me.molybdenum.ambience_mini.engine.utils.Pair;
import me.molybdenum.ambience_mini.engine.utils.Utils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MusicThread extends Thread
{
    private final Logger _logger;
    private final Supplier<Boolean> _isFocused;


    private final boolean _lostFocusEnabled;
    private final long _updateIntervalMilliseconds;
    private final long _nextMusicDelayMilliseconds;

    private final boolean _verboseMode;
    private List<Music> _currentPlaylist = null;
    private long _tick = 0L;


    private final Random _rand = new Random(System.nanoTime());
    private final Interpreter _playlistSelector;
    private final MusicProvider _musicProvider;

    private final boolean _meticulousPlaylistSelector;
    private final int _numLatestChoices = 3; // Code below is only designed to handle the value 3 here.
    private int _nextChoiceIndex = 0;
    private final PlaylistChoice[] _latestChoices = new PlaylistChoice[_numLatestChoices];

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


    private boolean _kill = false;


    @SuppressWarnings("rawtypes")
    public MusicThread(
        BaseCore baseCore, // Raw use of BaseCore since we do not need to know the types used in implementation.,
        Interpreter playlistSelector,
        MusicProvider musicProvider,
        Logger logger
    ) {
        _playlistSelector = playlistSelector;
        _musicProvider = musicProvider;
        _logger = logger;
        _isFocused = baseCore::isFocused;

        _lostFocusEnabled = baseCore.clientConfig.lostFocusEnabled.get();
        _updateIntervalMilliseconds = baseCore.clientConfig.updateInterval.get();
        _nextMusicDelayMilliseconds = baseCore.clientConfig.nextMusicDelay.get();
        _meticulousPlaylistSelector = baseCore.clientConfig.meticulousPlaylistSelector.get();

        _verboseMode = baseCore.clientConfig.verboseMode.get();

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
            VolumeState.registerVolumeHandler(_volumeChangedHandler);
            handleVolumeZero(VolumeState.getVolume());

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
            VolumeState.unregisterVolumeHandler(_volumeChangedHandler);
        }
    }

    public void kill()
    {
        if (isAlive()) {
            _kill = true;
            stopMainMusic(false);
            stopInterruptMusic(false);
            VolumeState.unregisterVolumeHandler(_volumeChangedHandler);

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
    private void handleMusicCycle()
    {
        ArrayList<Pair<String, Value>> trace = null;
        ArrayList<String> messages = null;
        if (_verboseMode) {
            trace = new ArrayList<>();
            messages = new ArrayList<>();
            _tick++;
        }

        _playlistSelector.prepare(messages);
        PlaylistChoice nextChoice = _meticulousPlaylistSelector
                ? selectPlaylistMeticulously(trace)
                : _playlistSelector.selectPlaylist(trace);
        if (nextChoice == null)
            return;

        List<Music> nextPlaylist = nextChoice.playlist();
        boolean nextIsInterrupt = nextChoice.isInterrupt();
        boolean doFade = !nextChoice.isInstant();

        if (_verboseMode) {
            if (_currentPlaylist != nextPlaylist) {
                _currentPlaylist = nextPlaylist;

                if (nextIsInterrupt)
                    _logger.info("At tick '{}'. Selected new interrupt playlist: [{}]", _tick, String.join(", ", nextPlaylist.stream().map(Music::musicPath).toList()));
                else
                    _logger.info("At tick '{}'. Selected new playlist: [{}]", _tick, String.join(", ", nextPlaylist.stream().map(Music::musicPath).toList()));
                _logger.info("Values computed during selection:\n{}", Utils.getKeyValuePairString(trace));
            }

            for (var msg : messages)
                _logger.info("At tick '{}'. {}", _tick, msg);
        }

        MusicPlayer activePlayer = nextIsInterrupt ? _interruptPlayer : _mainPlayer;
        Music currentMusic = activePlayer == null ? null : activePlayer.music;

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
                _interruptPlayer = playMusic(nextMusic, _musicProvider, doFade);
            } else {
                stopMainMusic(doFade);
                _mainPlayer = playMusic(nextMusic, _musicProvider, doFade);
            }
        }

        else if (!activePlayer.isPlaying())
            activePlayer.playOrResume(doFade);
    }

    private PlaylistChoice selectPlaylistMeticulously(ArrayList<Pair<String, Value>> trace) {
        _latestChoices[_nextChoiceIndex] = _playlistSelector.selectPlaylist(trace);
        _nextChoiceIndex = (_nextChoiceIndex + 1) % _numLatestChoices;

        // Hardcoded (for efficiency) majority vote between three latest choices.
        if (Objects.equals(_latestChoices[0], _latestChoices[1]) || Objects.equals(_latestChoices[0], _latestChoices[2]))
            return _latestChoices[0];
        else if (Objects.equals(_latestChoices[1], _latestChoices[2]))
            return _latestChoices[1];
        else
            return null;
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


    private MusicPlayer playMusic(Music nextMusic, MusicProvider musicProvider, boolean fade) {
        MusicPlayer musicPlayer = new MusicPlayer(
            nextMusic,
            VolumeState.getVolume(),
            musicProvider,
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
