package me.molybdenum.ambience_mini.engine.client.core.server_music;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.monitor.music_selector.values.MapVal;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.remote_music.RequestMusicChunkMessage;
import me.molybdenum.ambience_mini.engine.shared.music.streams.ManualPreAllocBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class ServerMusicCache
{
    // Core components
    private BaseClientNetworkManager network;
    private BaseClientConfig clientConfig;
    private ServerSetup serverSetup;

    // ServerMusic
    private MapVal serverPlaylists = null; // Map from 'string' to 'playlist'
    private final Map<String, Integer> musicPathToSize = new HashMap<>();
    private final ArrayList<MusicDataCache> cachedMusic = new ArrayList<>();

    private int memoryUsage = 0;
    private boolean isBuffering = false;

    // Loading and buffering
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> bufferFuture;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.network != null)
            throw new RuntimeException("Multiple calls to 'ServerMusicCache.init'!");

        network = core.networkManager;
        clientConfig = core.clientConfig;
        serverSetup = core.serverSetup;
    }

    private void clear() {
        serverPlaylists = null;
        cachedMusic.clear();
    }


    //------------------------------------------------------------------------------------------------------------------
    // Game state
    public MapVal getServerPlaylists() {
        return serverPlaylists;
    }


    //------------------------------------------------------------------------------------------------------------------
    // Server playlists

    // TODO: Use locks and condition variables to schedule one task in executor which requests and then waits for chunks
    //  of the remote data until all data is fetched or a timeout occurs (show error message on timeout).
    //  Once loaded, the executor can be used for buffering.



    //------------------------------------------------------------------------------------------------------------------
    // Music and Cache management
    public int getMusicSize(String musicPath) throws FileNotFoundException {
        var musicSize = musicPathToSize.get(musicPath);
        if (musicSize == null)
            throw new FileNotFoundException("Could not find the server-located music file '" + musicPath + "'");
        return musicSize;
    }

    public ManualPreAllocBuffer getMusicStream(@NotNull String musicPath) throws FileNotFoundException {
        return getCache(musicPath).orElse(createCache(musicPath)).musicData;
    }


    public int getMemoryUsage() {
        return memoryUsage;
    }

    private Optional<MusicDataCache> getCache(String musicPath) {
        synchronized (cachedMusic) {
            for (var cache : cachedMusic)
                if (cache.musicPath.equals(musicPath))
                    return Optional.of(cache);
            return Optional.empty();
        }
    }

    private MusicDataCache createCache(String musicPath) throws FileNotFoundException {
        synchronized (cachedMusic) {
            // TODO: Remove unused caches to free space if exceeding max memory

            var cache = new MusicDataCache(musicPath, getMusicSize(musicPath));
            cachedMusic.add(cache);
            memoryUsage += cache.musicSize();
            return cache;
        }
    }

    private boolean deleteCache(String musicPath) {
        synchronized (cachedMusic) {
            var it = cachedMusic.iterator();
            while (it.hasNext()) {
               var cache = it.next();
               if (cache.musicPath.equals(musicPath)) {
                   it.remove();
                   memoryUsage -= cache.musicSize();
                   return true;
               }
            }
            return false;
        }
    }


    private void ensureBuffering() {
        synchronized (cachedMusic) {
            if (!isBuffering)
                doBuffer();
        }
    }

    private void doBuffer() {
        synchronized (cachedMusic) {
            for (var cache : cachedMusic)
                if (!cache.isFullyLoaded()) {
                    network.sendToServer(new RequestMusicChunkMessage());

                    isBuffering = true;
                    return;
                }
            isBuffering = false;
        }
    }

    private void handleBufferPacket() {
        synchronized (cachedMusic) {

        }
    }


    private record MusicDataCache(@NotNull String musicPath, ManualPreAllocBuffer musicData) {
        private MusicDataCache(String musicPath, int musicSize) {
            this(musicPath, new ManualPreAllocBuffer(musicSize, 1000));
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
    }
}
