package me.molybdenum.ambience_mini.engine.server.core;

import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.server.core.locations.BaseStructureReader;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.music.ServerConfigInterpreter;
import me.molybdenum.ambience_mini.engine.server.core.music.ServerMusicManager;
import me.molybdenum.ambience_mini.engine.server.core.networking.BaseServerNetworkManager;
import me.molybdenum.ambience_mini.engine.server.core.misc.ServerNameCache;
import me.molybdenum.ambience_mini.engine.server.core.setup.BaseServerConfig;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.configuration.LoadResult;
import me.molybdenum.ambience_mini.engine.shared.configuration.Loader;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis.Setup;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.RealMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class BaseServerCore<
        TServerPlayer,
        TServerConfig extends BaseServerConfig,
        TNetworkManager extends BaseServerNetworkManager<TServerPlayer>,
        TStructureReader extends BaseStructureReader<TServerPlayer, ?, ?>
> {
    public static final RealMusicProvider localOnlyMusicProvider = new RealMusicProvider(Constants.musicDirPath.toString(), null);

    // Utils
    public final Logger logger;
    private final JobCenter jobCenter = JobCenter.singleThreaded();

    public final ServerNameCache nameCache;

    // Setup
    public final TServerConfig serverConfig;

    // Networking
    public final TNetworkManager networkManager;

    // Locations
    public final ServerAreaManager areaManager;
    public final TStructureReader structureReader;

    // Flags
    public final FlagManager flagManager;

    // Music
    public ServerMusicManager musicManager;


    public BaseServerCore(
            Logger logger,
            ServerNameCache nameCache,
            TServerConfig serverConfig,
            ServerAreaManager areaManager,
            TStructureReader structureReader,
            FlagManager flagManager,
            ServerMusicManager musicManager,
            TNetworkManager networkManager
    ) {
        this.logger = logger;

        this.nameCache = nameCache;
        this.serverConfig = serverConfig;
        this.areaManager = areaManager;
        this.structureReader = structureReader;
        this.flagManager = flagManager;
        this.musicManager = musicManager;
        this.networkManager = networkManager;
    }

    // NOTE: Init must be separate from initializer since extenders of ServerCore have additional initialization logic
    // in their own constructors and which some components depend on.
    public void init() {
        this.nameCache.init(this);
        this.areaManager.init(this);
        this.networkManager.init(this);
        this.flagManager.init(this);
        this.musicManager.init(this);

        loadMusicConfig();
    }

    private void loadMusicConfig() {
        File configFile = Constants.musicConfigPath.toFile();
        try (InputStream configStream = new FileInputStream(configFile)) {
            loadServerConfig(configStream)
                    .match(this::loadServerPlaylists, this::printErrors);
        } catch (IOException ignored) { }
    }

    private LoadResult<ServerConfigInterpreter> loadServerConfig(
            InputStream configStream
    ) {
        return Loader.loadAndValidateConfig(
                configStream,
                localOnlyMusicProvider,
                isDedicatedServer() ? new Setup.DedicatedServer() : new Setup.IntegratedServer()
        ).map(config -> new ServerConfigInterpreter(config, localOnlyMusicProvider));
    }

    private void loadServerPlaylists(ServerConfigInterpreter serverConfig, List<Message> warnings) {
        Utils.printMessages(logger, warnings);

        this.musicManager.loadServerPlaylists(serverConfig, localOnlyMusicProvider);

        logger.info("Successfully loaded server-sided Ambience Mini configuration");
    }

    private void printErrors(List<Message> messages) {
        logger.warn("Ambience Mini failed to load!");
        Utils.printMessages(logger, messages);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public Path getAmStoragePath() {
        Path storagePath = getWorldRootPath().resolve(Constants.AM_STORAGE_DIRECTORY);
        if (!storagePath.toFile().exists()) {
            try {
                Files.createDirectory(storagePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return storagePath;
    }


    public void onStarting() {
        this.nameCache.loadCache();
        this.areaManager.loadAllAreas();
        this.flagManager.loadFlags();

        this.flagManager.registerPeriodicTasks(jobCenter);
        this.musicManager.registerPeriodicTasks(jobCenter);
    }

    public void onStopped() {
        if (!jobCenter.shutdownAndAwaitTermination(10000))
            logger.warn("Background task executor refuses to shut down. Data might be lost!");

        this.nameCache.saveCache();
        this.areaManager.saveAllAreas();
        this.flagManager.saveFlags();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isDedicatedServer();

    public abstract Path getWorldRootPath();
}
