package me.molybdenum.ambience_mini.engine.server.core;

import me.molybdenum.ambience_mini.engine.server.core.locations.BaseStructureReader;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
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
        TStructureReader extends BaseStructureReader<TServerPlayer, ?, ?>,
        TAreaManager extends ServerAreaManager
> {
    // Utils
    public final Logger logger;
    public final ServerNameCache nameCache;

    // Locations
    public final TAreaManager areaManager;
    public final TStructureReader structureReader;

    // Networking
    public final TNetworkManager networkManager;


    public BaseServerCore(
            Logger logger,
            ServerNameCache nameCache,
            TAreaManager areaManager,
            TStructureReader structureReader,
            TNetworkManager networkManager
    ) {
        this.logger = logger;
        this.nameCache = nameCache;

        this.areaManager = areaManager;
        this.structureReader = structureReader;

        this.networkManager = networkManager;
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
        this.areaManager.loadAllAreas();
    }

    public void onStopping() {
        this.nameCache.saveCache();
        this.areaManager.saveAllAreas();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract Path getWorldRootPath();
}
