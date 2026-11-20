package me.molybdenum.ambience_mini.engine.client.core.misc;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.GetNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.NeoGetNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientNameCache
{
    // Core functionality
    private BaseClientNetworkManager network;
    private ServerSetup serverSetup;

    // Caching
    private String currentPlayerUUID;
    private String currentPlayerName;

    private final ConcurrentHashMap<String, String> playerNameCache = new ConcurrentHashMap<>();


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.network != null)
            throw new RuntimeException("Multiple calls to 'ClientNameCache.init'!");

        network = core.networkManager;
        serverSetup = core.serverSetup;
    }


    public void setCurrentPlayer(String uuid, String name) {
        this.currentPlayerUUID = uuid;
        this.currentPlayerName = name;

        if (uuid != null && name != null)
            putPlayerName(uuid, name);
    }

    public String getCurrentPlayerUUID() {
        return currentPlayerUUID;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }


    public void putPlayerName(String uuid, String name) {
        playerNameCache.put(uuid, name);
    }

    public String getPlayerName(String uuid) {
        return playerNameCache.computeIfAbsent(uuid, key -> {
            if (serverSetup.serverVersion.isGreaterThanOrEqual(AmVersion.V_2_8_0))
                network.configureAsync()
                        .onSuccess(bytes -> putPlayerName(uuid, new String(bytes)))
                        .onTimeout(() -> playerNameCache.remove(uuid))
                        .send(new NeoGetNameCacheMessage(uuid));
            else if (serverSetup.serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
                network.sendAsync(new GetNameCacheMessage(key));
            return "Loading...";
        });
    }

    public void clear() {
        playerNameCache.clear();
        this.currentPlayerUUID = null;
        this.currentPlayerName = null;
    }
}
