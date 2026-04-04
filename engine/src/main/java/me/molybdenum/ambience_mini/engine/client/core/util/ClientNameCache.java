package me.molybdenum.ambience_mini.engine.client.core.util;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.GetNameCacheMessage;

import java.util.HashMap;

public class ClientNameCache
{
    // Core functionality
    private BaseClientNetworkManager network;

    // Caching
    private String currentPlayerUUID;
    private String currentPlayerName;

    private final HashMap<String, String> playerNameCache = new HashMap<>();


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.network != null)
            throw new RuntimeException("Multiple calls to 'ClientNameCache.init'!");

        network = core.networkManager;
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
            network.sendToServer(new GetNameCacheMessage(key));
            return "Loading...";
        });
    }

    public void clear() {
        playerNameCache.clear();
        this.currentPlayerUUID = null;
        this.currentPlayerName = null;
    }
}
