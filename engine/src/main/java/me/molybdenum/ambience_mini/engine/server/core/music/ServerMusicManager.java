package me.molybdenum.ambience_mini.engine.server.core.music;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.PlaylistDTO;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.music.streams.LazyPreAllocBuffer;
import me.molybdenum.ambience_mini.engine.shared.music.streams.StreamPreAllocBuffer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServerMusicManager
{
    private final Object lock = new Object();
    private Logger logger;

    private BaseMusicProvider musicProvider;

    private ArrayList<PlaylistDTO> serverPlaylists = new ArrayList<>();
    private int serverPlaylistsByteSize;

    private final HashMap<String, Boolean> mayLoadCache = new HashMap<>();
    private final ArrayList<MusicDataCache> musicCache = new ArrayList<>();

    private ScheduledFuture<?> cleanupFuture;
    private long unusedThresholdMillis = 30_000;
    private int cleanupIntervalMillis = 10_000;

    private int memoryUsage = 0;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.logger != null)
            throw new RuntimeException("Multiple calls to 'ServerMusicManager.init'!");

        this.logger = core.logger;

        // TODO: Configurable threshold and interval
    }


    public void registerPeriodicTasks(ScheduledExecutorService executor) {
        cleanupFuture = executor.scheduleAtFixedRate(
                this::removeUnusedCaches,
                0, cleanupIntervalMillis, TimeUnit.MILLISECONDS
        );
    }

    public void stopPeriodicTasks() {
        cleanupFuture.cancel(false);
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

    public byte @Nullable [] getMusicData(String musicPath, int offset, int length) {
        if (!mayLoadFile(musicPath))
            return null;

        try {
            synchronized (lock) {
                var oCache = getCache(musicPath);
                return (oCache.isPresent() ? oCache.get() : createCache(musicPath))
                        .getMusicData().read(offset, length);
            }
        } catch (IOException e) {
            logger.warn("Could not read music file '{}'", musicPath, e);
            return null;
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
        // TODO: Remove unused caches to free space if exceeding max memory

        var cache = new MusicDataCache(musicPath);
        musicCache.add(cache);
        memoryUsage += cache.getMusicSize();

        return cache;
    }

    private void removeUnusedCaches() {
        long now = System.currentTimeMillis();
        synchronized (lock) {
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
