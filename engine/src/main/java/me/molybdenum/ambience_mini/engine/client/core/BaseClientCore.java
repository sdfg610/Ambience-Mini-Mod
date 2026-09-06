package me.molybdenum.ambience_mini.engine.client.core;

import me.molybdenum.ambience_mini.engine.client.core.music.music_selector.MusicSelector;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.client.core.music.ServerMusicCache;
import me.molybdenum.ambience_mini.engine.shared.configuration.LoadResult;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.*;
import me.molybdenum.ambience_mini.engine.client.core.flags.FlagCache;
import me.molybdenum.ambience_mini.engine.client.core.locations.areas.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.structures.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.misc.ClientNameCache;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.BuildConfig;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.configuration.Loader;
import me.molybdenum.ambience_mini.engine.client.core.providers.GameStateProviderReal;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseCombatState;
import me.molybdenum.ambience_mini.engine.client.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseScreenState;
import me.molybdenum.ambience_mini.engine.client.core.music.Monitor;
import me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis.Setup;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.RealMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.core.areas.AreaStorage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.ClientInfoMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.McVersion;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public abstract class BaseClientCore<
        TBlockPos, TVec3, TBlockState, TEntity, TKeyBinding, TComponent,
        TNotification extends BaseNotification<TComponent>,
        TNetworkManager extends BaseClientNetworkManager,
        TAreaRenderer extends BaseAreaRenderer<TVec3, TBlockPos, ?>, // Last type, namely TScreen, is never exposed to a public interface.
        TClientConfig extends BaseClientConfig,
        TKeyBindings extends BaseKeyBindings<TKeyBinding>,
        TPlayerState extends BasePlayerState<TBlockPos, TVec3, ?>,
        TLevelState extends BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity, ?>, // Last type, namely TClientLevel, is never exposed to a public interface.
        TScreenState extends BaseScreenState,
        TCombatState extends BaseCombatState<TEntity, TVec3>
> {
    private static boolean hasPrintedControls = false;

    // Utils
    public final McVersion mcVersion;
    public final Logger logger;
    public final ClientNameCache nameCache;
    public final StructureCache structureCache;
    public final TNotification notification;

    // Networking
    public final TNetworkManager networkManager;

    // Areas
    public final ClientAreaManager areaManager;
    public final TAreaRenderer areaRenderer;

    // Flags
    public final FlagCache flagCache;

    // Server music
    public final ServerMusicCache musicCache;

    // Setup
    public final ServerSetup serverSetup;
    public final TClientConfig clientConfig;
    public final TKeyBindings keyBindings;

    // State
    public final TPlayerState playerState;
    public final TLevelState levelState;
    public final TScreenState screenState;
    public final TCombatState combatState;

    private GameStateProviderReal<TBlockPos, TVec3, TBlockState, TEntity> gameStateProvider;

    // Music
    private final RealMusicProvider musicProvider;

    private final Object monitorLock = new Object();
    private Monitor monitor;


    public BaseClientCore(
            McVersion mcVersion,
            Logger logger,
            ClientNameCache nameCache,
            StructureCache structureCache,
            TNotification notification,
            TNetworkManager networkManager,
            ClientAreaManager areaManager,
            TAreaRenderer areaRenderer,
            FlagCache flagCache,
            ServerMusicCache musicCache,
            ServerSetup serverSetup,
            TClientConfig clientConfig,
            TKeyBindings keyBindings,
            TPlayerState playerState,
            TLevelState levelState,
            TScreenState screenState,
            TCombatState combatState
    ) {
        this.mcVersion = mcVersion;
        this.logger = logger;

        this.nameCache = nameCache;
        this.structureCache = structureCache;
        this.notification = notification;
        this.networkManager = networkManager;
        this.areaManager = areaManager;
        this.areaRenderer = areaRenderer;
        this.flagCache = flagCache;
        this.musicCache = musicCache;
        this.serverSetup = serverSetup;
        this.clientConfig = clientConfig;
        this.keyBindings = keyBindings;
        this.playerState = playerState;
        this.levelState = levelState;
        this.screenState = screenState;
        this.combatState = combatState;

        this.nameCache.init(this);
        this.structureCache.init(this);
        this.networkManager.init(this);
        this.areaManager.init(this);
        this.areaRenderer.init(this, levelState);
        this.flagCache.init(this);
        this.musicCache.init(this);
        this.keyBindings.init(this);
        this.screenState.init(this);
        this.combatState.init(this, playerState, levelState);

        this.musicProvider = new RealMusicProvider(Constants.musicDirPath.toString(), musicCache);
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isFocused();

    protected abstract String getWorldNameForLocalStorage();


    // -----------------------------------------------------------------------------------------------------------------
    // State
    public GameStateProviderReal<TBlockPos, TVec3, TBlockState, TEntity> getGameStateProvider() {
        if (gameStateProvider == null)
            gameStateProvider = new GameStateProviderReal<>(
                    mcVersion, this, playerState, levelState, combatState
            );
        return gameStateProvider;
    }

    public Monitor getMonitor() {
        return monitor;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Music engine
    public void onSoundEngineReloaded() {
        synchronized (monitorLock) {
            if (isMonitorRunning())
                Monitor.enableAutoRestart();
            else
                tryReloadMusicEngine();
        }
    }

    public void tryReloadMusicEngine() {
        synchronized (monitorLock) {
            if (monitor != null)
                monitor.stop();

            combatState.clearCombatants();
            gameStateProvider = new GameStateProviderReal<>(
                    mcVersion, this, playerState, levelState, combatState
            );

            File configFile = Constants.musicConfigPath.toFile();
            try (InputStream configStream = new FileInputStream(configFile)) {
                loadClientMusicSelector(configStream, musicProvider, gameStateProvider)
                        .match(this::initMusicThread, this::printErrors);
            } catch (IOException ignored) { }
        }
    }

    private void initMusicThread(MusicSelector musicSelector, List<Message> warnings) {
        Utils.printMessages(logger, warnings);

        monitor = new Monitor(this, musicSelector, musicProvider, logger);

        if (clientConfig.verboseMode.get())
            logger.info("Successfully loaded Ambience Mini with configuration:\n{}", clientConfig.getConfigsString());
        else
            logger.info("Successfully loaded Ambience Mini");
    }

    private void printErrors(List<Message> messages) {
        logger.warn("Ambience Mini failed to load! Errors are as follows:");
        Utils.printMessages(logger, messages);
    }

    public boolean isMonitorRunning() {
        return monitor != null && monitor.isRunning();
    }


    public static LoadResult<MusicSelector> loadClientMusicSelector(
            InputStream configStream,
            BaseMusicProvider musicProvider,
            BaseGameStateProvider gameStateProvider
    ) {
        return Loader.loadAndValidateConfig(
                configStream,
                musicProvider,
                new Setup.Client(gameStateProvider)
        ).map(config -> new MusicSelector(config, gameStateProvider));
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Common Handlers
    public void onLoggedIn(AmVersion serverVersion, boolean isOnLocalServer, String playerUUID, String playerName) {
        serverSetup.serverVersion = serverVersion;
        serverSetup.isOnLocalServer = isOnLocalServer;

        musicCache.clear();

        combatState.clearCombatants();
        structureCache.clear();
        nameCache.clear();
        nameCache.setCurrentPlayer(playerUUID, playerName);

        if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
            networkManager.sendAsync(new ClientInfoMessage(
                    BuildConfig.APP_VERSION.toString(),
                    playerUUID,
                    playerName
            ));

        areaRenderer.clear();
        String subFolder = serverSetup.isOnLocalServer ? "sp" : "mp";
        areaManager.loadAreas(new AreaStorage(logger, Path.of(Constants.AM_LOCAL_STORAGE_DIRECTORY, subFolder, getWorldNameForLocalStorage())));

        flagCache.clearAndLoadFlags();

        if (clientConfig.notifyServerSupport.get() && !isOnLocalServer) {
            if (serverVersion.isGreaterThanOrEqual(BuildConfig.APP_VERSION))
                notification.printTranslatableToChat(AmLang.MSG_FULL_SERVER_SUPPORT);
            else if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
                notification.printTranslatableToChat(AmLang.MSG_PARTIAL_SERVER_SUPPORT);
            else
                notification.printTranslatableToChat(AmLang.MSG_NO_SERVER_SUPPORT);
        }

        if (!hasPrintedControls && clientConfig.printBasicControls.get()) {
            notification.printTranslatableToChat(AmLang.MSG_BASIC_CONTROLS,
                keyBindings.getKeyString(keyBindings.reloadKey),
                keyBindings.getKeyString(keyBindings.playPauseKey),
                keyBindings.getKeyString(keyBindings.nextMusicKey),
                keyBindings.getKeyString(keyBindings.printAllKey)
            );
            hasPrintedControls = true;
        }
    }

    public void onLoggedOut() {
        structureCache.clear();
        nameCache.clear();
        areaRenderer.clear();
        flagCache.clear();
        musicCache.clear();

        serverSetup.reset();
        combatState.clearCombatants();
    }
}