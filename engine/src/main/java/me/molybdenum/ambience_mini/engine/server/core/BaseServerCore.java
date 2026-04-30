package me.molybdenum.ambience_mini.engine.server.core;

import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.server.core.locations.BaseStructureReader;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.networking.BaseServerNetworkManager;
import me.molybdenum.ambience_mini.engine.server.core.util.ServerNameCache;
import me.molybdenum.ambience_mini.engine.shared.Common;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    // Flags
    public final FlagManager flagManager;

    // Networking
    public final TNetworkManager networkManager;


    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> saveAreasFuture;


    public BaseServerCore(
            Logger logger,
            ServerNameCache nameCache,
            TAreaManager areaManager,
            TStructureReader structureReader,
            FlagManager flagManager,
            TNetworkManager networkManager
    ) {
        this.logger = logger;

        this.nameCache = nameCache;
        this.areaManager = areaManager;
        this.structureReader = structureReader;
        this.flagManager = flagManager;
        this.networkManager = networkManager;
    }

    public void init() {
        this.nameCache.init(this);
        this.areaManager.init(this);
        this.networkManager.init(this);
        this.flagManager.init(this);
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
        this.flagManager.loadFlags();

        saveAreasFuture = executor.scheduleAtFixedRate(
                flagManager::saveFlags,
                0,
                60,
                TimeUnit.SECONDS
        );
    }

    public void onStopping() {
        saveAreasFuture.cancel(false);
        executor.shutdown();

        this.nameCache.saveCache();
        this.areaManager.saveAllAreas();
        this.flagManager.saveFlags();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract Path getWorldRootPath();
}
