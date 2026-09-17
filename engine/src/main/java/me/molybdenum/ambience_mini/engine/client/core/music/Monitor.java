package me.molybdenum.ambience_mini.engine.client.core.music;

import me.molybdenum.ambience_mini.engine.client.core.music.music_selector.selection.*;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicPlayer;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.client.core.music.music_selector.MusicSelector;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.client.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Supplier;

public class Monitor
{
    public static final int JOB_STARTUP_DELAY_MS = 1000;

    public static final int BUFFER_UPDATE_INTERVAL_MS = 500;
    public static final int AUTO_RESTART_GRACE_PERIOD = 10_000;

    // Utils
    private final Random _rand = new Random(System.nanoTime());
    private final Logger _logger;

    // Core components
    @SuppressWarnings("rawtypes")
    private final BaseClientCore _core;
    private final BaseNotification<?> _notification;
    private final BaseKeyBindings<?> _keyBindings;

    // Music
    private final MusicPlayer musicPlayer;

    // Tasks
    private static long latestAutoRestartTime = 0;

    private final JobCenter jobCenter = JobCenter.pooled(1);
    private final MusicCycleJob musicCycleJob;



    @SuppressWarnings("rawtypes")
    public Monitor(
            BaseClientCore clientCore, // Raw use of BaseCore since we do not need to know the types used in implementation.
            MusicSelector playlistSelector,
            BaseMusicProvider musicProvider,
            Logger logger
    ) {
        _logger = logger;

        _core = clientCore;
        _notification = _core.notification;
        _keyBindings = _core.keyBindings;

        // Setup scheduled tasks
        musicCycleJob = jobCenter.schedule(
                new MusicCycleJob(playlistSelector, _core::isFocused),
                JOB_STARTUP_DELAY_MS, _core.clientConfig.updateInterval.get()
        );
        jobCenter.schedule(
                new MusicBufferJob(),
                JOB_STARTUP_DELAY_MS, BUFFER_UPDATE_INTERVAL_MS
        );

        // Setup music player and volume
        musicPlayer = new MusicPlayer(
                musicProvider,
                () -> musicCycleJob.selectNewMusic(_core.clientConfig.nextMusicDelay.get())
        );
        musicPlayer.setVolume(VolumeState.getMusicVolume());
        musicPlayer.addNowPlayingListener(nowPlaying -> {
            try {
                if (nowPlaying.music().locatedOnServer()) {
                    var res = _core.musicCache.bufferThis(nowPlaying.path());
                    if (res.isFailure())
                        _notification.printToChat(res.error);
                }
            } catch (FileNotFoundException ignored) { }

            if (_core.clientConfig.printNowPlaying.get()) {
                if (nowPlaying.author() == null)
                    _notification.showToast(AmLang.MSG_PLAYING_NAME, nowPlaying.titleOrPath());
                else
                    _notification.showToast(AmLang.MSG_PLAYING_NAME_AUTHOR, nowPlaying.titleOrPath(), nowPlaying.author());
            }
        });

        VolumeState.registerVolumeListener(musicCycleJob::updateVolume);
        musicCycleJob.updateVolume();
    }


    public boolean isVanillaPlayerSelected() {
        return musicCycleJob.vanillaPlayerSelected;
    }

    public List<String> getActiveSoundtracks() {
        return musicPlayer.getActiveSoundtracks();
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Thread control
    private void attemptAutoRestart() {
        try {
            stop();

            long now = System.currentTimeMillis();
            if (now - latestAutoRestartTime > AUTO_RESTART_GRACE_PERIOD) {
                latestAutoRestartTime = now;

                _notification.printTranslatableToChat(AmLang.MSG_PLAYER_AUTO_RESTARTING);
                _notification.showToast(AmLang.MSG_PLAYER_AUTO_RESTARTING);
                _core.tryReloadMusicEngine();
            }
            else {
                _notification.printTranslatableToChat(AmLang.MSG_PLAYER_CRASHED, _keyBindings.getReloadKeyString());
                _notification.showToast(AmLang.MSG_PLAYER_CRASHED, _keyBindings.getReloadKeyString());
            }
        } catch (Exception e) {
            throw new RuntimeException("En exception occurred while attempting to auto restart the music player.", e);
        }
    }

    public void stop() {
        synchronized (jobCenter) {
            if (!jobCenter.isShutdown()) {
                jobCenter.shutdown();
                VolumeState.unregisterVolumeListener(musicCycleJob::updateVolume);
                musicPlayer.stopAll();
            }
        }
    }

    public boolean isRunning() {
        return !jobCenter.isShutdown();
    }

    public static void enableAutoRestart() {
        latestAutoRestartTime = 0;
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Manual control
    public void pause() {
        musicCycleJob.pause();
    }

    public void resume() {
        musicCycleJob.resume();
    }

    public boolean isPaused() {
        return musicCycleJob.isPaused();
    }

    public void forceSelectNewMusic() {
        musicCycleJob.selectNewMusic(0);
    }



    private class MusicCycleJob extends JobCenter.Job
    {
        private static final int NUM_MEASUREMENTS = 10;

        // Core components
        private final BasePlayerState<?, ?, ?> playerState;
        private final BaseLevelState<?, ?, ?, ?, ?> levelState;

        // Music
        private final boolean useMeticulousPlaylistSelector;
        private final boolean fadeOnLostFocus;
        private final boolean doFadeOnJukebox;

        private final MusicSelector playlistSelector;
        private List<Music> currentPlaylist = null;
        private boolean vanillaPlayerSelected = false;
        private long _chooseNextMusicTime = 0L;

        private final int _numLatestChoices = 3; // Code below is only designed to handle the value 3 here.
        private int _nextChoiceIndex = 0;
        private final Selection[] _latestChoices = new Selection[_numLatestChoices];

        // Music control
        private final Supplier<Boolean> isFocused;
        private boolean isPaused = false; // By player/user
        private boolean isHalted = false; // By game state or configuration

        private boolean volumeZero = false;

        // Debugging
        private final boolean verboseMode;
        private long _tick = 0L;

        private final long[] _benchmarks = new long[NUM_MEASUREMENTS];
        private long _benchmarkTime = 0;
        private int _benchmarkIndex = 0;

        private boolean latestSelectionWasUndefined;


        public MusicCycleJob(MusicSelector playlistSelector, Supplier<Boolean> isFocused) {
            this.playlistSelector = playlistSelector;
            this.isFocused = isFocused;

            this.playerState = _core.playerState;
            this.levelState = _core.levelState;

            // Music config
            this.useMeticulousPlaylistSelector = _core.clientConfig.meticulousPlaylistSelector.get();
            this.fadeOnLostFocus = _core.clientConfig.lostFocusEnabled.get();
            this.doFadeOnJukebox = _core.clientConfig.fadeOnJukebox.get();

            // Debug config
            this.verboseMode = _core.clientConfig.verboseMode.get();
        }


        public void updateVolume() {
            float musicVolume = VolumeState.getMusicVolume();
            this.volumeZero = musicVolume < .005f || VolumeState.getMasterVolume() < .005f;
            musicPlayer.setVolume(volumeZero ? 0f : musicVolume);
        }

        public void selectNewMusic(long delayMillis) {
            _chooseNextMusicTime = System.currentTimeMillis() + delayMillis;
        }

        public void pause() {
            isPaused = true;
        }

        public void resume() {
            isPaused = false;
        }

        public boolean isPaused() {
            return isPaused;
        }


        // ----------------------------------------------------------------------------------------------------------------
        // Playlist selection
        protected void body() {
            if (handleVolumeZero() || handleUnfocused())
                return;

            ArrayList<Pair<String, Value<?>>> trace = null;
            ArrayList<String> messages = null;
            if (verboseMode) {
                trace = new ArrayList<>();
                messages = new ArrayList<>();
                _tick++;
                _benchmarkTime = System.currentTimeMillis();
            }

            playlistSelector.prepare(messages);
            if (handleJukebox())
                return;

            Selection selection = useMeticulousPlaylistSelector
                    ? selectPlaylistMeticulously(trace)
                    : playlistSelector.selectPlaylist(trace);
            if (selection instanceof NoneSelection || (verboseMode && handleUndefinedSelection(selection)))
                return;

            boolean oldVanillaPlayerSelected = vanillaPlayerSelected;
            vanillaPlayerSelected = selection instanceof VanillaSelection;
            if (verboseMode && !oldVanillaPlayerSelected && vanillaPlayerSelected)
                _logger.info("At tick '{}', on line '{}'. Enabled the vanilla music player", _tick, ((VanillaSelection)selection).line());
            if (vanillaPlayerSelected) {
                musicPlayer.pause(true);
                return;
            }

            if (handlePaused()) // Placed here since pause does not affect vanilla player
                return;         // so vanilla player should be able to activate even when AM is paused.

            @SuppressWarnings("DataFlowIssue")
            PlaylistSelection nextChoice = (PlaylistSelection)selection;

            List<Music> nextPlaylist = nextChoice.playlist();
            boolean doFade = !nextChoice.isInstant();
            int nextPriority = nextChoice.priority();

            if (verboseMode) {
                long selectTime = System.currentTimeMillis() - _benchmarkTime;
                _benchmarks[_benchmarkIndex] = selectTime;
                _benchmarkIndex = (_benchmarkIndex + 1) % NUM_MEASUREMENTS;

                if (currentPlaylist != nextPlaylist) {
                    currentPlaylist = nextPlaylist;

                    var playlist = String.join(", ", nextPlaylist.stream().map(m -> '"' + m.path() + '"').toList());
                    _logger.info("At tick '{}', on line '{}'. Selected new playlist at priority '{}': [ {} ]", _tick, nextChoice.line(), nextPriority, playlist);
                    _logger.info("Events and properties computed during selection:\n{}", Utils.getKeyValuePairString(trace));
                    _logger.info("Playlist selection took {}ms. The average time is currently {}ms.", selectTime, Arrays.stream(_benchmarks).average().orElse(Double.MIN_VALUE));
                }

                for (var msg : messages)
                    _logger.info("At tick '{}'. {}", _tick, msg);
            }

            Music currentMusic = musicPlayer.getMusicByPriority(nextPriority);
            boolean musicStillValid = currentMusic != null && nextPlaylist.stream().anyMatch(currentMusic::equals);

            if (isHalted) {
                if (verboseMode)
                    _logger.info("Resuming music!");
                isHalted = false;

                if (musicStillValid) {
                    musicPlayer.stopAllAbove(nextPriority, false);
                    musicPlayer.resume(true);
                    return;
                }
            }

            Optional<Integer> topPriority = musicPlayer.getTopPriority();
            boolean presentAndPlaying = topPriority.isPresent() && musicPlayer.isPlaying();
            if (presentAndPlaying && nextPriority != topPriority.get()) {
                musicPlayer.pause(doFade);
                if (doFade) // If faded (which takes some time), start over and recheck if we still need to change music.
                    return; // If not, we can just go directly to the new music selection.
            }

            if (nextPlaylist.isEmpty()) {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.stopAllAbove(nextPriority - 1, doFade);
                    musicPlayer.pause(doFade);
                }
            }

            else if (!musicStillValid || System.currentTimeMillis() > _chooseNextMusicTime){
                musicPlayer.play(nextPriority, selectMusic(nextPlaylist, currentMusic), doFade);
                _chooseNextMusicTime = Long.MAX_VALUE;
            }

            else if (!musicPlayer.isPlaying())
                musicPlayer.resume(nextPriority, doFade);
        }

        private @NotNull Selection selectPlaylistMeticulously(ArrayList<Pair<String, Value<?>>> trace) {
            _latestChoices[_nextChoiceIndex] = playlistSelector.selectPlaylist(trace);
            _nextChoiceIndex = (_nextChoiceIndex + 1) % _numLatestChoices;

            // Hardcoded (for efficiency) majority vote between three latest choices.
            if (Objects.equals(_latestChoices[0], _latestChoices[1]) || Objects.equals(_latestChoices[0], _latestChoices[2]))
                return _latestChoices[0];
            else if (Objects.equals(_latestChoices[1], _latestChoices[2]))
                return _latestChoices[1];
            else
                return new NoneSelection();
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

        private boolean handleUndefinedSelection(Selection selection) {
            var gotUndefined = selection instanceof UndefinedSelection;
            if (!latestSelectionWasUndefined && gotUndefined)
                _logger.info("At tick '{}', on line '{}'. Current music continues after selecting undefined playlist", _tick, ((UndefinedSelection) selection).line());
            latestSelectionWasUndefined = gotUndefined;
            return gotUndefined;
        }


        // ----------------------------------------------------------------------------------------------------------------
        // Music player controls
        private boolean handleVolumeZero() {
            if (volumeZero)
                haltMusic(false);
            return volumeZero;
        }

        private boolean handleUnfocused() {
            if (fadeOnLostFocus) {
                boolean isUnfocused = !isFocused.get();
                if (isUnfocused)
                    haltMusic(true);
                return isUnfocused;
            }
            return false;
        }

        private boolean handleJukebox() {
            if (doFadeOnJukebox && playerState.notNull()) {
                boolean isJukeboxPlaying = playerState.canHearJukeboxMusic() && !levelState.isWorldTickingPaused();
                if (isJukeboxPlaying)
                    haltMusic(true);
                return isJukeboxPlaying;
            }
            return false;
        }

        private boolean handlePaused() {
            if (isPaused)
                haltMusic(true);
            return isPaused;
        }

        private void haltMusic(boolean doFade) {
            if (!isHalted) {
                if (verboseMode)
                    _logger.info("Halting music!");
                musicPlayer.pause(doFade);
                isHalted = true;
            }
        }


        @Override
        protected void onException(Exception ex) {
            _logger.error("Error in music cycle!", ex);
            attemptAutoRestart();
        }
    }

    private class MusicBufferJob extends JobCenter.Job
    {
        @Override
        protected void body() {
            musicPlayer.updateBuffers();
        }

        @Override
        protected void onException(Exception ex) {
            _logger.error("Error in music buffering!", ex);
            attemptAutoRestart();
        }
    }
}
