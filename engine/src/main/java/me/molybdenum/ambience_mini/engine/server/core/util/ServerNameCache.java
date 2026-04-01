package me.molybdenum.ambience_mini.engine.server.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static me.molybdenum.ambience_mini.engine.shared.Common.NAME_CACHE_FILE_NAME;


public class ServerNameCache {
    private final HashMap<String, String> playerNameCache = new HashMap<>();

    private Path cacheFilePath;
    private Logger logger;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.cacheFilePath != null)
            throw new RuntimeException("Multiple calls to 'ServerNameCache.init'!");
        this.cacheFilePath = core.getAmStoragePath().resolve(NAME_CACHE_FILE_NAME);
        this.logger = core.logger;
    }


    public void putPlayerName(String uuid, String name) {
        String oldName = playerNameCache.put(uuid, name);
        if (!name.equals(oldName))
            saveCache();
    }

    public String getPlayerName(String uuid) {
        return playerNameCache.getOrDefault(uuid, "N/A");
    }


    public void saveCache() {
        JsonArray cache = new JsonArray(playerNameCache.size());
        playerNameCache.forEach((uuid, name) -> {
            var entry = new JsonObject();
            entry.add("uuid", new JsonPrimitive(uuid));
            entry.add("name", new JsonPrimitive(name));
            cache.add(entry);
        });

        try {
            Files.writeString(cacheFilePath, cache.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String err = "Could not save name-cache to '" + cacheFilePath.toString() + "'";
            logger.error(err, e);
        }
    }
    
    public void loadCache() {
        playerNameCache.clear();
        if (!cacheFilePath.toFile().exists())
            return;

        try {
            JsonArray cache = JsonParser.parseReader(Files.newBufferedReader(cacheFilePath, StandardCharsets.UTF_8)).getAsJsonArray();
            cache.forEach(elem -> playerNameCache.put(
                    elem.getAsJsonObject().get("uuid").getAsString(),
                    elem.getAsJsonObject().get("name").getAsString()
            ));
        } catch (Exception e) {
            String err = "Could not load name-cache to '" + cacheFilePath.toString() + "'";
            logger.error(err, e);
        }
    }
}
