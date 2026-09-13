package me.molybdenum.ambience_mini.engine.server.core.music;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.PlaylistDTO;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.music.streams.LazyPreAllocBuffer;
import me.molybdenum.ambience_mini.engine.shared.music.streams.StreamPreAllocBuffer;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import me.molybdenum.ambience_mini.engine.shared.utils.results.TextResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ServerMusicManager
{
    private final Object lock = new Object();
    private Logger logger;

    private BaseMusicProvider musicProvider;

    // Playlists
    private ArrayList<PlaylistDTO> serverPlaylists;
    private int serverPlaylistsByteSize;

    // Caching
    private final HashMap<String, Boolean> mayLoadCache = new HashMap<>();
    private final ArrayList<MusicDataCache> musicCache = new ArrayList<>();

    private int memoryUsage = 0;
    private int maxMemoryUsage;

    private long unusedThresholdMillis;
    private int cleanupIntervalMillis;



    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.logger != null)
            throw new RuntimeException("Multiple calls to 'ServerMusicManager.init'!");

        this.logger = core.logger;

        this.maxMemoryUsage = core.serverConfig.musicCacheMaxMemory.get() * 1_000_000; // "* 1_000_000" to get in bytes and not megabytes
        this.unusedThresholdMillis = core.serverConfig.musicCacheUnusedThreshold.get();
        this.cleanupIntervalMillis = core.serverConfig.musicCacheCleanupInterval.get();
    }


    public void registerPeriodicTasks(JobCenter executor) {
        executor.schedule(
                JobCenter.Job.of(this::removeUnusedCaches), 0, cleanupIntervalMillis
        );
    }


    //------------------------------------------------------------------------------------------------------------------
    // Server playlists
    public void loadServerPlaylists(ServerConfigInterpreter serverConfig, BaseMusicProvider musicProvider) {
        this.musicProvider = musicProvider;

        try {
            serverPlaylists = serverConfig.loadServerPlaylists();
            serverPlaylistsByteSize = serverPlaylists.stream()
                    .mapToInt(PlaylistDTO::getSerializedLength)
                    .sum();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasServerPlaylists() {
        return serverPlaylists != null;
    }

    public int getServerPlaylistCount() {
        return serverPlaylists.size();
    }

    public int getServerPlaylistByteSize() {
        return serverPlaylistsByteSize;
    }

    public List<PlaylistDTO> getServerPlaylistChunk(int offset, int byteSizeSoftLimit) {
        var totalSize = new int[] { 0 };
        return serverPlaylists.stream()
                .skip(offset)
                .takeWhile(dto -> {
                    var take = totalSize[0] <= byteSizeSoftLimit;
                    totalSize[0] += dto.getSerializedLength();
                    return take;
                })
                .toList();
    }


    //------------------------------------------------------------------------------------------------------------------
    // Music and cache
    public int getMemoryUsage() {
        synchronized (lock) {
            return memoryUsage;
        }
    }

    public @NotNull TextResult<byte[]> getMusicData(String musicPath, int offset, int length) {
        try {
            synchronized (lock) {
                if (!mayLoadFile(musicPath))
                    return TextResult.fail(Text.ofTranslatable(AmLang.MSG_SERVER_MUSIC_READ_FAIL, musicPath));

                var oCache = getCache(musicPath);
                var cache = oCache.isPresent() ? oCache.get() : createCache(musicPath);
                if (cache == null)
                    return TextResult.fail(Text.ofTranslatable(AmLang.MSG_MUSIC_CACHE_OUT_OF_MEMORY));
                return TextResult.of(cache.getMusicData().read(offset, length));
            }
        } catch (IOException e) {
            logger.warn("Could not read music file '{}'", musicPath, e);
            return TextResult.fail(Text.ofTranslatable(AmLang.MSG_SERVER_MUSIC_READ_FAIL, musicPath));
        }
    }

    private boolean mayLoadFile(String musicPath) {
        return mayLoadCache.computeIfAbsent(musicPath, ignored ->
                serverPlaylists.stream().anyMatch(pl -> pl.music().stream().anyMatch(music -> music.musicPath().equals(musicPath)))
                        && musicProvider.existsLocally(musicPath)
        );
    }

    private Optional<MusicDataCache> getCache(String musicPath) {
        for (var cache : musicCache)
            if (cache.musicPath.equals(musicPath))
                return Optional.of(cache);
        return Optional.empty();
    }

    private MusicDataCache createCache(String musicPath) throws FileNotFoundException {
        removeUnusedCaches();

        int musicSize = musicProvider.getLocalMusicSize(musicPath);
        if (musicSize > maxMemoryUsage - memoryUsage)
            return null;

        var cache = new MusicDataCache(musicPath);
        musicCache.add(cache);
        memoryUsage += cache.getMusicSize();
        return cache;
    }

    private void removeUnusedCaches() {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            var it = musicCache.iterator();
            while (it.hasNext()) {
                var cache = it.next();
                if (now - cache.getLatestAccess() > unusedThresholdMillis) {
                    it.remove();
                    memoryUsage -= cache.getMusicSize();
                    try {
                        cache.close();
                    } catch (IOException ignored) {  }
                }
            }
        }
    }


    private class MusicDataCache {
        public final String musicPath;
        private final LazyPreAllocBuffer musicData;

        private long latestAccess = System.currentTimeMillis();


        private MusicDataCache(String musicPath) throws FileNotFoundException {
            this.musicPath = musicPath;
            this.musicData = new StreamPreAllocBuffer(
                    musicProvider.getLocalMusicStream(musicPath),
                    musicProvider.getLocalMusicSize(musicPath),
                    1024*5
            );
        }


        public int getMusicSize() {
            return musicData.getBufferSize();
        }

        public LazyPreAllocBuffer getMusicData() {
            latestAccess = System.currentTimeMillis();
            return musicData;
        }

        public long getLatestAccess() {
            return latestAccess;
        }

        public void close() throws IOException {
            musicData.close();
        }
    }
}
