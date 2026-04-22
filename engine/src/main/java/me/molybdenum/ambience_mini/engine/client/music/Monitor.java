package me.molybdenum.ambience_mini.engine.client.music;

import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.client.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Monitor
{
    public static final int BUFFER_UPDATE_INTERVAL_MS = 500;
    private static final int NUM_MEASUREMENTS = 10;

    // Utils
    private final Random _rand = new Random(System.nanoTime());
    private final Logger _logger;

    // Core components
    private final BasePlayerState<?, ?, ?> _player;
    private final BaseLevelState<?, ?, ?, ?, ?> _level;
    private final BaseNotification<?> _notification;
    private final BaseKeyBindings<?> _keyBindings;

    // Playlist/music selection
    private final Interpreter _playlistSelector;
    private long _chooseNextMusicTime = 0L;

    private final boolean _meticulousPlaylistSelector;
    private final int _numLatestChoices = 3; // Code below is only designed to handle the value 3 here.
    private int _nextChoiceIndex = 0;
    private final PlaylistChoice[] _latestChoices = new PlaylistChoice[_numLatestChoices];

    // Music player
    private final MusicPlayer _musicPlayer;
    private boolean _isPaused = false; // By player/user
    private boolean _isHalted = false; // By game state or configuration

    private final Supplier<Boolean> _isFocused;
    private final boolean _lostFocusEnabled;
    private final boolean _doFadeOnJukebox;

    // Volume
    private boolean _volumeZero = false;
    private final Consumer<Float> _volumeChangedHandler;

    // Debugging
    private final boolean _verboseMode;
    private List<Music> _currentPlaylist = null;
    private long _tick = 0L;
    private long _benchmarkTime = 0;
    private int _benchmarkIndex = 0;
    private final long[] _benchmarks = new long[NUM_MEASUREMENTS];

    // Tasks
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final ScheduledFuture<?> cycleFuture;
    private final ScheduledFuture<?> bufferFuture;


    @SuppressWarnings("rawtypes")
    public Monitor(
            BaseClientCore clientCore, // Raw use of BaseCore since we do not need to know the types used in implementation.
            Interpreter playlistSelector,
            MusicProvider musicProvider,
            Logger logger
    ) {
        _playlistSelector = playlistSelector;
        _logger = logger;
        _isFocused = clientCore::isFocused;

        _player = clientCore.playerState;
        _level = clientCore.levelState;
        _notification = clientCore.notification;
        _keyBindings = clientCore.keyBindings;

        _doFadeOnJukebox = clientCore.clientConfig.fadeOnJukeBox.get();
        _lostFocusEnabled = clientCore.clientConfig.lostFocusEnabled.get();
        _meticulousPlaylistSelector = clientCore.clientConfig.meticulousPlaylistSelector.get();

        _verboseMode = clientCore.clientConfig.verboseMode.get();

        // Setup music player and volume
        long nextMusicDelay = clientCore.clientConfig.nextMusicDelay.get();
        _musicPlayer = new MusicPlayer(
                musicProvider,
                () -> _chooseNextMusicTime = System.currentTimeMillis() + nextMusicDelay
        );
        _musicPlayer.setVolume(VolumeState.getMusicVolume());

        _volumeChangedHandler = volume -> {
            handleVolumeZero(volume);
            _musicPlayer.setVolume(volume);
        };
        VolumeState.registerMusicVolumeListener(_volumeChangedHandler);

        // Setup scheduled tasks
        handleVolumeZero(VolumeState.getMusicVolume());
        cycleFuture = executor.scheduleAtFixedRate(
                () -> guarded(this::handleMusicCycle),
                0,
                clientCore.clientConfig.updateInterval.get(),
                TimeUnit.MILLISECONDS
        );
        bufferFuture = executor.scheduleAtFixedRate(
                () -> guarded(_musicPlayer::updateBuffers),
                0,
                BUFFER_UPDATE_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Thread control
    public void guarded(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            _notification.printTranslatableToChat(AmLang.MSG_PLAYER_CRASHED, _keyBindings.getReloadKeyString());
            _logger.error("Error in monitor!", ex);
            stop();
        }
    }

    public void stop() {
        synchronized (executor) {
            if (!executor.isShutdown()) {
                _musicPlayer.stopAll();

                cycleFuture.cancel(true);
                bufferFuture.cancel(true);
                executor.shutdown();

                VolumeState.unregisterVolumeListener(_volumeChangedHandler);
            }
        }
    }

    public boolean isRunning() {
        return !executor.isShutdown();
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Manual pausing
    public void pause() {
        _isPaused = true;
    }

    public void resume() {
        _isPaused = false;
    }

    public boolean isPaused() {
        return _isPaused;
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Music and volume
    private void handleMusicCycle() {
        if (_volumeZero || handlePaused() || handleUnfocused())
            return;

        ArrayList<Pair<String, Value<?>>> trace = null;
        ArrayList<String> messages = null;
        if (_verboseMode) {
            trace = new ArrayList<>();
            messages = new ArrayList<>();
            _tick++;
            _benchmarkTime = System.currentTimeMillis();
        }

        _playlistSelector.prepare(messages);
        if (handleJukebox())
            return;

        PlaylistChoice nextChoice = _meticulousPlaylistSelector
                ? selectPlaylistMeticulously(trace)
                : _playlistSelector.selectPlaylist(trace);
        if (nextChoice == null)
            return;

        List<Music> nextPlaylist = nextChoice.playlist();
        int nextPriority = nextChoice.isInterrupt() ? 1 : 0;
        boolean doFade = !nextChoice.isInstant();

        if (_verboseMode) {
            long selectTime = System.currentTimeMillis() - _benchmarkTime;
            _benchmarks[_benchmarkIndex] = selectTime;
            _benchmarkIndex = (_benchmarkIndex + 1) % NUM_MEASUREMENTS;

            if (_currentPlaylist != nextPlaylist) {
                _currentPlaylist = nextPlaylist;

                var playlist = String.join(", ", nextPlaylist.stream().map(m -> '"' + m.musicPath() + '"').toList());
                if (nextPriority == 1)  // TODO: Better logging when priority system is done
                    _logger.info("At tick '{}'. Selected new interrupt playlist: [ {} ]", _tick, playlist);
                else
                    _logger.info("At tick '{}'. Selected new playlist: [ {} ]", _tick, playlist);
                _logger.info("Values computed during selection:\n{}", Utils.getKeyValuePairString(trace));
                _logger.info("Playlist selection took {}ms. The average time is currently {}ms.", selectTime, Arrays.stream(_benchmarks).average().orElse(Double.MIN_VALUE));
            }

            for (var msg : messages)
                _logger.info("At tick '{}'. {}", _tick, msg);
        }

        Music currentMusic = _musicPlayer.getMusicByPriority(nextPriority);
        boolean musicStillValid = currentMusic != null && nextPlaylist.stream().anyMatch(currentMusic::equals);

        if (_isHalted) {
            if (_verboseMode)
                _logger.info("Resuming music!");
            _isHalted = false;

            if (musicStillValid) {
                _musicPlayer.stopAllAbove(nextPriority, false);
                _musicPlayer.resume(true);
                return;
            }
        }

        var topPriority = _musicPlayer.getTopPriority();
        boolean presentAndPlaying = topPriority.isPresent() && _musicPlayer.isPlaying();
        if (presentAndPlaying && nextPriority > topPriority.get())
            _musicPlayer.pause(doFade);

        else if (presentAndPlaying && nextPriority < topPriority.get())
            _musicPlayer.pause(doFade);

        else if (nextPlaylist.isEmpty()) {
            if (_musicPlayer.isPlaying()) {
                _musicPlayer.stopAllAbove(nextPriority - 1, doFade);
                _musicPlayer.pause(doFade);
            }
        }

        else if (!musicStillValid || System.currentTimeMillis() > _chooseNextMusicTime)
            playMusic(nextPriority, selectMusic(nextPlaylist, currentMusic), doFade);

        else if (!_musicPlayer.isPlaying())
                _musicPlayer.resume(nextPriority, doFade);
    }

    private PlaylistChoice selectPlaylistMeticulously(ArrayList<Pair<String, Value<?>>> trace) {
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



    // ----------------------------------------------------------------------------------------------------------------
    // Music player controls
    private void handleVolumeZero(float volume) {
        _volumeZero = volume < .01f;
        if (_volumeZero)
            _musicPlayer.stopAll();
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

    private boolean handleJukebox() {
        if (_doFadeOnJukebox && _player.notNull() && _level.notNull()) {
            boolean isJukeboxPlaying = _player.canHearJukeboxMusic() && !_level.isWorldTickingPaused();
            if (isJukeboxPlaying && !_isHalted)
                haltMusic();
            return isJukeboxPlaying;
        }
        return false;
    }

    private boolean handlePaused() {
        if (_isPaused && !_isHalted)
            haltMusic();
        return _isPaused;
    }


    private void playMusic(int priority, Music nextMusic, boolean doFade) {
        _musicPlayer.play(priority, nextMusic, doFade);
        _chooseNextMusicTime = Long.MAX_VALUE;
    }

    private void haltMusic() {
        if (!_isHalted && _verboseMode)
            _logger.info("Halting music!");

        _isHalted = true;
        _musicPlayer.pause(true);
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Utilities

    // The most random number generator I could think of.
    // "_rand.nextInt" alone just didn't... feel random...
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
