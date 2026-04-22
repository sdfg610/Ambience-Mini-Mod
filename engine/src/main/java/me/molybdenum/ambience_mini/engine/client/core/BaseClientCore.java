package me.molybdenum.ambience_mini.engine.client.core;

import me.molybdenum.ambience_mini.engine.client.core.locations.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.util.ClientNameCache;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.BuildConfig;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.client.configuration.Loader;
import me.molybdenum.ambience_mini.engine.client.configuration.errors.ExcError;
import me.molybdenum.ambience_mini.engine.client.configuration.errors.LoadError;
import me.molybdenum.ambience_mini.engine.client.configuration.errors.SemError;
import me.molybdenum.ambience_mini.engine.client.configuration.errors.SynError;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.FileMusicProvider;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.core.providers.GameStateProviderReal;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseCombatState;
import me.molybdenum.ambience_mini.engine.client.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseScreenState;
import me.molybdenum.ambience_mini.engine.client.music.Monitor;
import me.molybdenum.ambience_mini.engine.shared.areas.AreaStorage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.ClientInfoMessage;
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
    private static final MusicProvider musicProvider = new FileMusicProvider(Path.of(Common.AMBIENCE_MUSIC_DIRECTORY, Common.MUSIC_DIRECTORY).toString());

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
        this.keyBindings.init(this);
        this.combatState.init(this, playerState, levelState);
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isFocused();

    protected abstract void disableNativeMusicManager();

    protected abstract String getWorldNameForLocalStorage();


    // -----------------------------------------------------------------------------------------------------------------
    // State
    public GameStateProviderReal<TBlockPos, TVec3, TBlockState, TEntity> getGameStateProvider() {
        return gameStateProvider;
    }

    public Monitor getMusicThread() {
        return monitor;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Music engine
    public void tryReloadMusicEngine()
    {
        if (monitor != null)
            monitor.stop();

        combatState.clearCombatants();
        gameStateProvider = new GameStateProviderReal<>(
                mcVersion, this, playerState, levelState, combatState
        );

        File configFile = Path.of(Common.AMBIENCE_MUSIC_DIRECTORY, Common.MUSIC_CONFIG_FILE).toFile();
        try (InputStream configStream = new FileInputStream(configFile)) {
            Loader.loadFrom(configStream, musicProvider, gameStateProvider).match(
                    this::initMusicThread,
                    this::printErrors
            );
        } catch (IOException ignored) { }
    }

    private void initMusicThread(Interpreter interpreter) {
        disableNativeMusicManager();
        monitor = new Monitor(this, interpreter, musicProvider, logger);

        if (clientConfig.verboseMode.get())
            logger.info("Successfully loaded Ambience Mini with configuration:\n{}", clientConfig.getConfigsString());
        else
            logger.info("Successfully loaded Ambience Mini");
    }

    private void printErrors(List<LoadError> errors) {
        for (var error : errors) {
            if (error instanceof SynError err)
                logger.error("Syntactic error [line {}, column {}]: {}", err.line(), err.column(), err.message());
            else if (error instanceof SemError err)
                logger.error("Semantic error [line {}]: {}", err.line(), err.message());
            else if (error instanceof ExcError err)
                logger.error("An exception occurred while loading the music configuration:\n", err.exception());
            else
                throw new RuntimeException("Could not print load-error of type: " + error.getClass().getName());
        }
    }

    public boolean isMusicThreadRunning() {
        return monitor != null && monitor.isRunning();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Common Handlers
    public void onLoggedIn(AmVersion serverVersion, boolean isOnLocalServer, String playerUUID, String playerName) {
        serverSetup.serverVersion = serverVersion;
        serverSetup.isOnLocalServer = isOnLocalServer;

        structureCache.clear();
        nameCache.clear();
        nameCache.setCurrentPlayer(playerUUID, playerName);

        if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
            networkManager.sendToServer(new ClientInfoMessage(
                    BuildConfig.APP_VERSION.toString(),
                    playerUUID,
                    playerName
            ));

        areaRenderer.clear();
        String subFolder = serverSetup.isOnLocalServer ? "sp" : "mp";
        areaManager.loadAreas(new AreaStorage(logger, Path.of(Common.AM_LOCAL_STORAGE_DIRECTORY, subFolder, getWorldNameForLocalStorage())));

        if (clientConfig.notifyServerSupport.get() && !isOnLocalServer) {
            if (serverVersion.isGreaterThanOrEqual(BuildConfig.APP_VERSION))
                notification.printTranslatableToChat(AmLang.MSG_FULL_SERVER_SUPPORT);
            else if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
                notification.printTranslatableToChat(AmLang.MSG_PARTIAL_SERVER_SUPPORT);
            else
                notification.printTranslatableToChat(AmLang.MSG_NO_SERVER_SUPPORT);
        }
    }

    public void onLoggedOut() {
        structureCache.clear();
        nameCache.clear();

        serverSetup.reset();
        combatState.clearCombatants();
    }
}