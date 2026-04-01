package me.molybdenum.ambience_mini.engine.server.core;

import me.molybdenum.ambience_mini.engine.server.core.areas.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.networking.BaseServerNetworkManager;
import me.molybdenum.ambience_mini.engine.server.core.util.ServerNameCache;
import me.molybdenum.ambience_mini.engine.shared.Common;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseServerCore<
        TServerPlayer,
        TNetworkManager extends BaseServerNetworkManager<TServerPlayer>,
        TAreaManager extends ServerAreaManager
> {
    // Utils
    public final Logger logger;
    public final ServerNameCache nameCache;

    // Networking
    public final TNetworkManager networkManager;
    public final TAreaManager areaManager;


    public BaseServerCore(
            Logger logger,
            ServerNameCache nameCache,
            TNetworkManager networkManager,
            TAreaManager areaManager
    ) {
        this.logger = logger;
        this.nameCache = nameCache;

        this.networkManager = networkManager;
        this.areaManager = areaManager;
    }

    public void init() {
        this.nameCache.init(this);

        this.networkManager.init(this);
        this.areaManager.init(this);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public Path getAmStoragePath() {
        Path storagePath = getWorldRootPath().resolve(Common.AM_STORAGE_DIRECTORY);
        if (!storagePath.toFile().exists()) {
            try {
                Files.createDirectory(storagePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return storagePath;
    }


    public void onStarted() {
        this.nameCache.loadCache();
    }

    public void onStopping() {
        this.nameCache.saveCache();
        // TODO: Save areas
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract Path getWorldRootPath();
}
