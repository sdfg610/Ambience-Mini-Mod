package me.molybdenum.ambience_mini.engine.client.core.music;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.PlaylistVal;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.StringVal;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.helpers.ValueMap;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.MapVal;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music.RequestMusicChunkMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music.RequestServerPlaylistChunkMessage;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.MusicDTO;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.PlaylistDTO;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.ValueDTO;
import me.molybdenum.ambience_mini.engine.shared.music.streams.ManualPreAllocBuffer;
import me.molybdenum.ambience_mini.engine.shared.utils.Lazy;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import me.molybdenum.ambience_mini.engine.shared.utils.results.TextResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Supplier;

public class ServerMusicCache
{
    private final Object lock = new Object();

    private final JobCenter jobCenter = JobCenter.singleThreaded();

    // Core components
    private Logger logger;

    private BaseClientNetworkManager network;
    private ServerSetup serverSetup;
    private BaseNotification<?> notification;

    private Supplier<List<String>> getActiveSoundtracks;

    // Playlists
    private int maxPlaylistsByteSize;
    private long playlistChunkTimeoutMillis;

    private int maxMusicByteSize;
    private long musicChunkTimeoutMillis;

    private ValueMap serverPlaylists; // Map from 'string' to 'playlist'
    private Map<String, Integer> musicPathToSize;

    private LoadPlaylistsJob loadPlaylistsJob;

    // Music
    private final ArrayList<MusicDataCache> cachedMusic = new ArrayList<>();
    private int memoryUsage = 0;
    private int maxMemoryUsage;

    private BufferMusicJob bufferMusicJob;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.network != null)
            throw new RuntimeException("Multiple calls to 'ServerMusicCache.init'!");

        logger = core.logger;

        network = core.networkManager;
        serverSetup = core.serverSetup;
        notification = core.notification;

        this.getActiveSoundtracks = () -> {
            var mon = core.getMonitor();
            return mon == null ? List.of() : mon.getActiveSoundtracks();
        };

        this.maxMemoryUsage = core.clientConfig.maxMusicCacheSize.get() * 1_000_000; // MB to bytes

        this.maxPlaylistsByteSize = core.clientConfig.maxPlaylistsSize.get() * 1_000_000; // MB to bytes
        this.playlistChunkTimeoutMillis = core.clientConfig.playlistChunkTimeout.get();

        this.maxMusicByteSize = core.clientConfig.maxMusicSize.get() * 1_000_000; // MB to bytes
        this.musicChunkTimeoutMillis = core.clientConfig.musicChunkTimeout.get();
    }


    public boolean isLoaded() {
        return serverPlaylists != null;
    }

    public void clear() {
        synchronized (lock) {
            serverPlaylists = null;
            musicPathToSize = null;
            cachedMusic.clear();
            memoryUsage = 0;

            jobCenter.cancelAll(true);
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // Server playlists
    public MapVal getServerPlaylists() {
        synchronized (lock) {
            return new MapVal(serverPlaylists);
        }
    }

    public void startLoadServerPlaylists(int playlistsByteSize, int playlistCount) {
        synchronized (lock) {
            if (loadPlaylistsJob == null && serverPlaylists == null) {
                if (playlistsByteSize <= maxPlaylistsByteSize)
                    loadPlaylistsJob = jobCenter.post(new LoadPlaylistsJob(playlistCount));
                else {
                    notification.showToast(AmLang.MSG_SERVER_PLAYLISTS_SKIPPED);
                    logger.info("Size of server playlist data ({} bytes) exceeds the limit of {} bytes (can be changed in mod-config). Will not load server playlists.", playlistsByteSize, maxPlaylistsByteSize);
                }
            }
        }
    }



    //------------------------------------------------------------------------------------------------------------------
    // Music and Cache management
    public int getMemoryUsage() {
        synchronized (lock) {
            return memoryUsage;
        }
    }

    public int getMusicSize(String musicPath) throws FileNotFoundException {
        synchronized (lock) {
            if (serverPlaylists == null)
                throw new FileNotFoundException("Server playlists are not loaded!");

            var musicSize = musicPathToSize.get(musicPath);
            if (musicSize == null)
                throw new FileNotFoundException("Could not find the server-located music file '" + musicPath + "'");
            return musicSize;
        }
    }

    public ManualPreAllocBuffer getMusicBuffer(@NotNull String musicPath) throws FileNotFoundException {
        synchronized (lock) {
            if (serverPlaylists == null)
                throw new FileNotFoundException("Server playlists are not loaded!");
            var res = getOrCreateCache(musicPath);
            if (res.isFailure()) {
                notification.printToChat(res.error);
                throw new RuntimeException("Could not get or create music buffer!");
            }
            var cache = res.value;
                bufferThisOrAny(cache);
            return cache.getMusicData();
        }
    }

    private TextResult<MusicDataCache> getOrCreateCache(String musicPath) throws FileNotFoundException {
        var oCache = getCache(musicPath);
        return (oCache.isPresent() ? TextResult.of(oCache.get()) : createCache(musicPath));
    }

    private Optional<MusicDataCache> getCache(String musicPath) {
        for (var cache : cachedMusic)
            if (cache.musicPath.equals(musicPath))
                return Optional.of(cache);
        return Optional.empty();
    }

    private TextResult<MusicDataCache> createCache(String musicPath) throws FileNotFoundException {
        var size = getMusicSize(musicPath);
        if (!freeMemory(size)) {
            cachedMusic.clear();
            bufferMusicJob.cancel(true);
            return TextResult.fail(Text.ofTranslatable(AmLang.MSG_SERVER_MUSIC_CACHE_OUT_OF_MEMORY));
        }

        var cache = new MusicDataCache(musicPath, getMusicSize(musicPath));
        cachedMusic.add(cache);
        memoryUsage += cache.musicSize();

        return TextResult.of(cache);
    }

    private boolean freeMemory(int requiredSpace) {
        var tracks = new Lazy<>(getActiveSoundtracks);
        while (requiredSpace > maxMemoryUsage - memoryUsage) {
            MusicDataCache cacheToRemove = null;

            for (var cache : cachedMusic)
                if (!tracks.get().contains(cache.musicPath)) {
                    if (cacheToRemove == null || cache.getLatestAccessTime() < cacheToRemove.getLatestAccessTime())
                        cacheToRemove = cache;
                }

            if (cacheToRemove == null)
                return false;
            deleteCache(cacheToRemove);
        }
        return true;
    }

    private void deleteCache(String musicPath) {
        synchronized (lock) {
            cachedMusic.stream()
                    .filter(cache -> cache.musicPath.equals(musicPath))
                    .findFirst()
                    .ifPresent(this::deleteCache);
        }
    }

    private void deleteCache(MusicDataCache cache) {
        synchronized (lock) {
            cachedMusic.remove(cache);
            if (bufferMusicJob != null)
                bufferMusicJob.cancelIfHasCache(cache);
            memoryUsage -= cache.musicSize();
        }
    }


    public TextResult<Void> bufferThis(String musicPath) throws FileNotFoundException {
        synchronized (lock) {
            return getOrCreateCache(musicPath).map(cache -> {
                buffer(cache);
                return null;
            });
        }
    }

    public void bufferAny() {
        synchronized (lock) {
            if (bufferMusicJob == null || bufferMusicJob.isFullyLoaded()) {
                cachedMusic.stream()
                        .filter(c -> !c.isFullyLoaded())
                        .findFirst()
                        .ifPresent(this::buffer);
            }
        }
    }

    private void bufferThisOrAny(MusicDataCache cache) {
        if (!buffer(cache))
            bufferAny(); // If buffering "musicPath" does not require buffering. Just buffer anything.
    }

    private boolean buffer(MusicDataCache cache) {
        if (!cache.isFullyLoaded() && (bufferMusicJob == null || bufferMusicJob.getCache() != cache)) {
            if (bufferMusicJob != null)
                bufferMusicJob.cancel(false);
            bufferMusicJob = new BufferMusicJob(cache);
            jobCenter.post(bufferMusicJob);
            return true;
        }
        return false;
    }


    private static class MusicDataCache {
        public final String musicPath;
        private final ManualPreAllocBuffer musicData;

        private long latestAccessTime;


        private MusicDataCache(@NotNull String musicPath, ManualPreAllocBuffer musicData) {
            this.musicPath = musicPath;
            this.musicData = musicData;
        }

        private MusicDataCache(String musicPath, int musicSize) {
            this(musicPath, new ManualPreAllocBuffer(musicSize, 3000));
        }


        public int musicSize() {
            return musicData.getBufferSize();
        }

        public boolean isFullyLoaded() {
            return musicData.isFullyLoaded();
        }

        public int getBytesLoaded() {
            return musicData.getBytesLoaded();
        }


        public ManualPreAllocBuffer getMusicData() {
            latestAccessTime = System.currentTimeMillis();
            return musicData;
        }

        public long getLatestAccessTime() {
            return latestAccessTime;
        }
    }


    private final class LoadPlaylistsJob extends JobCenter.Job {
        private final ValueMap newServerPlaylists = new ValueMap();
        private final HashMap<String, Integer> newMusicPathToSize = new HashMap<>();
        private final ArrayList<Pair<String, Integer>> skippedMusic = new ArrayList<>();

        private final int playlistCount;


        public LoadPlaylistsJob(int playlistCount) {
            this.playlistCount = playlistCount;
        }


        @Override
        protected void body() {
            // Server playlists
            int retries = 0;
            int loadedPlaylists = 0;
            while (!isCancelled() && loadedPlaylists < playlistCount) {
                var res = network.sendSync(
                        new RequestServerPlaylistChunkMessage(loadedPlaylists, Constants.PLAYLIST_CHUNK_BYTE_SIZE),
                        playlistChunkTimeoutMillis
                );

                if (res == null) {
                    if (retries++ >= 5) {
                        notification.printTranslatableToChat(AmLang.MSG_SERVER_PLAYLIST_TIMEOUT);
                        logger.warn("Fetching of server playlists timed out. Server playlist loading aborted.");
                        cancel(false);
                    }
                } else if (res.isSuccess()) {
                    var playlists = res.decodeList(PlaylistDTO::new);
                    if (playlists.isEmpty()) {
                        notification.printLiteralToChat("Received empty playlist chunk. This should not happen. Please report this error.");
                        cancel(false);
                        break;
                    }
                    loadedPlaylists += playlists.size();
                    for (var pl : playlists)
                        loadPlaylist(pl);
                    retries = 0;
                }
                else {
                    assert res.error != null;
                    notification.printTranslatableToChat(AmLang.MSG_SERVER_PLAYLIST_FETCH_ERROR);
                    notification.printToChat(res.error);
                    cancel(false);
                }
            }

            if (!isCancelled() && !skippedMusic.isEmpty()) {
                notification.printTranslatableToChat(AmLang.MSG_SERVER_MUSIC_SKIPPED);
                var musicList = String.join("\n",
                        skippedMusic.stream().map(pair -> "-- '" + pair.left() + "' (size = " + pair.right() + " bytes)").toList()
                );
                logger.warn("The following server-located music files will not be used due to exceeding the size limit of {} bytes (can be changed in mod-config):\n{}", maxMusicByteSize, musicList);
            }

            synchronized (lock) {
                if (!isCancelled()) {
                    musicPathToSize = newMusicPathToSize;
                    serverPlaylists = newServerPlaylists;
                }
                loadPlaylistsJob = null;
            }
        }

        private void loadPlaylist(PlaylistDTO playlistDTO) {
            var music = playlistDTO.music().stream()
                    .map(this::loadAndGetMusic)
                    .filter(Objects::nonNull)
                    .toList();
            if (!music.isEmpty())
                newServerPlaylists.put(new StringVal(playlistDTO.key()), new PlaylistVal(music));
        }

        private Music loadAndGetMusic(MusicDTO musicDTO) {
            String musicPath = musicDTO.musicPath();
            int musicSize = musicDTO.musicSize();
            if (musicSize > maxMusicByteSize) {
                skippedMusic.add(new Pair<>(musicPath, musicSize));
                return null;
            }

            var gain = musicDTO.tryGetFlag(Music.ARG_GAIN).map(ValueDTO::tryGetFloat).orElse(0f);
            boolean loop = musicDTO.tryGetFlag(Music.ARG_LOOP).map(ValueDTO::tryGetBoolean).orElse(false);
            int loopStart = musicDTO.tryGetFlag(Music.ARG_LOOPSTART).map(ValueDTO::tryGetInt).orElse(-1);
            int loopEnd = musicDTO.tryGetFlag(Music.ARG_LOOPEND).map(ValueDTO::tryGetInt).orElse(-1);
            int loopLength = musicDTO.tryGetFlag(Music.ARG_LOOPLENGTH).map(ValueDTO::tryGetInt).orElse(-1);

            newMusicPathToSize.putIfAbsent(musicPath, musicSize);
            // TODO: locatedOnServer == true only if not on integrated server
            return new Music(
                    musicPath,
                    true || !serverSetup.isOnLocalServer,
                    gain,
                    loop, loopStart, loopEnd, loopLength
            );
        }
    }

    private final class BufferMusicJob extends JobCenter.Job {
        private final MusicDataCache cache;


        public BufferMusicJob(MusicDataCache cache) {
            this.cache = cache;
        }


        public MusicDataCache getCache() {
            return cache;
        }

        public boolean isFullyLoaded() {
            return cache.isFullyLoaded();
        }

        public void cancelIfHasCache(MusicDataCache cache) {
            if (this.cache == cache)
                cancel(true);
        }


        @Override
        protected void body() {
            int retries = 0;
            String musicPath = cache.musicPath;
            while (!isCancelled() && !cache.isFullyLoaded()) {
                int offset = cache.getBytesLoaded();
                int length = Math.min(Constants.MUSIC_CHUNK_BYTE_SIZE, cache.musicSize() - offset);

                var res = network.sendSync(
                        new RequestMusicChunkMessage(musicPath, offset, length),
                        musicChunkTimeoutMillis
                );

                if (res == null) {
                    if (retries++ >= 5) {
                        notification.printTranslatableToChat(AmLang.MSG_SERVER_MUSIC_TIMEOUT);
                        cancel(false);
                        break;
                    }
                } else if (res.isSuccess()) {
                    try {
                        assert res.data != null;
                        cache.musicData.writeToBuffer(res.data);
                        retries = 0;
                    } catch (Exception ex) {
                        notification.printTranslatableToChat(AmLang.MSG_SERVER_MUSIC_FETCH_ERROR);
                        logger.error("Error while fetching server-located music:", ex);
                        deleteCache(musicPath);
                        break;
                    }
                }
                else {
                    assert res.error != null;
                    notification.printTranslatableToChat(AmLang.MSG_SERVER_MUSIC_FETCH_ERROR);
                    notification.printToChat(res.error);
                    deleteCache(musicPath);
                    break;
                }
            }

            bufferAny();
        }
    }
}
