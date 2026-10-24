package me.molybdenum.ambience_mini.engine.shared.core.music.info;

import com.google.gson.*;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music.RequestServerMusicInfoMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper.HelperReader;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class MusicInfoCache
{
    private static final int TIMEOUT_MILLIS = 20_000;
    private final Path cachePath = Constants.musicPackPath.resolve("musicInfoCache.json");
    
    private Logger logger;

    private final JobCenter jobCenter = JobCenter.singleThreaded();

    // Client stuff
    private final Object clientLock = new Object();
    private boolean cacheDirty;

    private final HashMap<String, String> pathToHash = new HashMap<>();
    private final HashMap<String, MusicInfo> hashToInfo = new HashMap<>();

    private final HashMap<String, ArrayList<Consumer<MusicInfo>>> pathToCallbacks = new HashMap<>();
    private final HashMap<String, ArrayList<Consumer<MusicInfo>>> hashToCallbacks = new HashMap<>();

    // Server stuff
    private final Object serverLock = new Object();
    private BaseClientNetworkManager networkManager;

    private final HashMap<String, MusicInfo> serverPathToInfo = new HashMap<>();

    private final HashMap<String, ArrayList<Consumer<MusicInfo>>> serverPathToCallbacks = new HashMap<>();


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.networkManager != null)
            throw new RuntimeException("Multiple calls to '" + this.getClass().getName() + ".init'!");

        logger = core.logger;
        networkManager = core.networkManager;
    }


    public MusicInfo getLocalMusicInfoBlocking(MusicInstance inst) {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        try {
            lock.lock();
            MusicInfo[] info = { null };
            getLocalMusicInfo(inst, newInfo -> {
                info[0] = newInfo;
                try {
                    lock.lock();
                    cond.signalAll();
                } finally {
                    lock.unlock();
                }
            });
            while (info[0] == null) {
                if (!cond.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                    throw new RuntimeException("Reading of music info (such as author, duration, etc) timed out!");
            }
            return info[0];
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public MusicInfo getLocalMusicInfo(MusicInstance inst) {
        MusicInfo info = getLocalMusicInfoInner(inst, null);
        return info == null ? MusicInfo.EMPTY : info;
    }

    public void getLocalMusicInfo(MusicInstance inst, Consumer<MusicInfo> callback) {
        MusicInfo info = getLocalMusicInfoInner(inst, callback);
        if (info != null)
            callback.accept(info);
    }

    private MusicInfo getLocalMusicInfoInner(MusicInstance inst, Consumer<MusicInfo> callback) {
        MusicInfo info;
        synchronized (clientLock) {
            var hash = getHash(inst, callback);
            info = hash == null ? null : getInfo(inst, hash, callback);
        }
        return info;
    }


    /// Assumes clientLock acquired
    private String getHash(MusicInstance inst, Consumer<MusicInfo> callback) {
        var path = inst.music.path();

        var hash = pathToHash.get(path);
        if (hash == null) {
            var callbacks = pathToCallbacks.computeIfAbsent(path, ignored -> {
                jobCenter.post(new GetHashJob(inst));
                return new ArrayList<>();
            });
            if (callback != null)
                callbacks.add(callback);
        }

        return hash;
    }

    /// Assumes clientLock acquired
    private List<Consumer<MusicInfo>> putHash(String path, String hash) {
        pathToHash.put(path, hash);
        return pathToCallbacks.remove(path);
    }


    /// Assumes clientLock acquired
    private MusicInfo getInfo(MusicInstance inst, String hash, Consumer<MusicInfo> callback) {
        var info = hashToInfo.get(hash);
        if (info == null) {
            var callbacks = hashToCallbacks.computeIfAbsent(hash, ignored -> {
               jobCenter.post(new GetInfoJob(inst, hash));
               return new ArrayList<>();
            });
            if (callback != null)
                callbacks.add(callback);
        }

        return info;
    }

    /// Assumes clientLock acquired
    private List<Consumer<MusicInfo>> putInfo(String hash, MusicInfo musicInfo) {
        hashToInfo.put(hash, musicInfo);
        return hashToCallbacks.remove(hash);
    }


    public MusicInfo getServerMusicInfoBlocking(String path) {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        try {
            lock.lock();
            MusicInfo[] info = { null };
            getServerMusicInfo(path, newInfo -> {
                info[0] = newInfo;
                try {
                    lock.lock();
                    cond.signalAll();
                } finally {
                    lock.unlock();
                }
            });
            while (info[0] == null) {
                if (!cond.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                    throw new RuntimeException("Reading of server music info (such as author, duration, etc) timed out!");
            }
            return info[0];
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public MusicInfo getServerMusicInfo(String path) {
        MusicInfo info = getServerMusicInfoInner(path, null);
        return info == null ? MusicInfo.EMPTY : info;
    }

    public void getServerMusicInfo(String path, Consumer<MusicInfo> callback) {
        MusicInfo info = getServerMusicInfoInner(path, callback);
        if (info != null)
            callback.accept(info);
    }

    private MusicInfo getServerMusicInfoInner(String path, Consumer<MusicInfo> callback) {
        MusicInfo info;
        synchronized (serverLock) {
            info = serverPathToInfo.get(path);
            if (info == null) {
                var callbacks = serverPathToCallbacks.computeIfAbsent(path, ignored -> {
                    networkManager.configureAsync()
                            .setTimeout(TIMEOUT_MILLIS)
                            .onSuccess(data -> {
                                var musicInfo = parseMusicInfo(data);
                                List<Consumer<MusicInfo>> callbacks2;
                                synchronized (serverLock) {
                                    callbacks2 = serverPathToCallbacks.remove(path);
                                    serverPathToInfo.put(path, musicInfo);
                                }
                                for (var cb : callbacks2)
                                    cb.accept(musicInfo);
                            })
                            .onTimeout(() -> {
                                synchronized (serverLock) {
                                    serverPathToInfo.remove(path);
                                    serverPathToCallbacks.remove(path);
                                }
                            })
                            .send(new RequestServerMusicInfoMessage(path));
                    return new ArrayList<>();
                });
                if (callback != null)
                    callbacks.add(callback);
            }
        }

        return info;
    }

    private MusicInfo parseMusicInfo(byte[] data) {
        var res = MusicInfo.tryOfJson(JsonParser.parseString(new HelperReader(data).readString()));
        return res.map(Pair::right).orElse(MusicInfo.EMPTY);
    }


    // TODO: Periodic save and load on music player reload

    public void loadCache() {
        synchronized (clientLock) {
            jobCenter.cancelAll(true);
            pathToHash.clear();
            hashToInfo.clear();
            pathToCallbacks.clear();
            hashToCallbacks.clear();

            try {
                JsonElement json = JsonParser.parseReader(Files.newBufferedReader(cachePath, StandardCharsets.UTF_8));
                if (json.isJsonArray()) {
                    AtomicBoolean hasErroneousElements = new AtomicBoolean(false);
                    for (var elem : json.getAsJsonArray())
                        MusicInfo.tryOfJson(elem).ifPresentOrElse(
                                pair -> hashToInfo.put(pair.left(), pair.right()),
                                () -> hasErroneousElements.set(true)
                        );

                    if (hasErroneousElements.get())
                        logger.error("Malformed entries in duration-cache file '{}'", cachePath);
                } else
                    logger.error("Malformed duration-cache file '{}'", cachePath);
            } catch (Exception e) {
                logger.error("Failed to load duration-cache from file '{}'", cachePath);
            }

            cacheDirty = false;
        }
    }


    public void saveCache() {
        synchronized (clientLock) {
            if (!cacheDirty)
                return;

            JsonArray cache = new JsonArray();
            hashToInfo.forEach((hash, info) -> cache.add(info.toJson(hash)));
            writeCacheToFile(cache);

            cacheDirty = false;
        }
    }

    private void writeCacheToFile(JsonArray cache) {
        if (!cache.isEmpty()) {
            try {
                if (Constants.musicPackPath.toFile().exists()) // If the music pack folder exists
                    Files.writeString(cachePath, cache.toString(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                String err = "Could not save duration-cache to: " + cachePath;
                logger.error(err, e);
            }
        }
        else if (cachePath.toFile().exists()) {
            try {
                Files.delete(cachePath);
            } catch (Exception e) {
                String err = "Could not delete duration-cache from: " + cachePath;
                logger.error(err, e);
            }
        }
    }


    public void clearServerCache() {
        synchronized (serverLock) {
            serverPathToInfo.clear();
            serverPathToCallbacks.clear();
        }
    }


    private class GetHashJob extends JobCenter.Job
    {
        private final MusicInstance inst;


        public GetHashJob(MusicInstance inst) {
            this.inst = inst;
        }


        @Override
        protected void body() {
            try (var stream = inst.createStream()) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String hash = bytesToHex(digest.digest(stream.readAllBytes()));

                List<Consumer<MusicInfo>> callbacks = null;
                MusicInfo info = null;
                synchronized (clientLock) {
                    if (!isCancelled()) {
                        callbacks = putHash(inst.music.path(), hash);
                        info = getInfo(inst, hash, null);
                        if (info == null && callbacks != null)
                            hashToCallbacks.get(hash).addAll(callbacks);
                    }
                }
                if (info != null && callbacks != null)
                    for (var callback : callbacks)
                        callback.accept(info);
            } catch (NoSuchAlgorithmException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static String bytesToHex(byte[] hash) {
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }


        @Override
        protected void onException(Exception e) {
            super.onException(e);
        }
    }


    private class GetInfoJob extends JobCenter.Job
    {
        private final MusicInstance inst;
        private final String hash;


        public GetInfoJob(MusicInstance inst, String hash) {
            this.inst = inst;
            this.hash = hash;
        }


        @Override
        protected void body() {
            MusicInfo info = AmDecoder.infoOf(inst);

            List<Consumer<MusicInfo>> callbacks = null;
            synchronized (clientLock) {
                if (!isCancelled())
                    callbacks = putInfo(hash, info);
            }
            if (callbacks != null)
                for (var callback : callbacks)
                    callback.accept(info);
        }

        @Override
        protected void onException(Exception e) {
            super.onException(e);
        }
    }
}
