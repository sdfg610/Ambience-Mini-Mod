package me.molybdenum.ambience_mini.engine.client.core;

import me.molybdenum.ambience_mini.engine.client.core.areas.BaseClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
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
import me.molybdenum.ambience_mini.engine.client.core.providers.GameStateProviderV1Real;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.client.core.caves.CaveDetector;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseCombatState;
import me.molybdenum.ambience_mini.engine.client.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseScreenState;
import me.molybdenum.ambience_mini.engine.client.music.MusicThread;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.ModVersionMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public abstract class BaseClientCore<
        TBlockPos, TVec3, TBlockState, TEntity, TKeyBinding, TComponent,
        TNotification extends BaseNotification<TComponent>,
        TNetworkManager extends BaseClientNetworkManager,
        TAreaManager extends BaseClientAreaManager,
        TAreaRenderer extends BaseAreaRenderer<TVec3, TBlockPos>,
        TClientConfig extends BaseClientConfig,
        TKeyBindings extends BaseKeyBindings<TKeyBinding>,
        TPlayerState extends BasePlayerState<TBlockPos, TVec3>,
        TLevelState extends BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity>,
        TScreenState extends BaseScreenState,
        TCombatState extends BaseCombatState<TEntity, TVec3>
> {
    private static final MusicProvider musicProvider = new FileMusicProvider(Path.of(Common.AMBIENCE_DIRECTORY, Common.MUSIC_DIRECTORY).toString());

    // Utils
    public final Logger logger;
    public final TNotification notification;

    // Networking
    public final TNetworkManager networkManager;

    // Areas
    public final TAreaManager areaManager;
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

    private GameStateProviderV1Real<TBlockPos, TVec3, TBlockState, TEntity> gameStateProvider;

    // Music
    private MusicThread musicThread;


    public BaseClientCore(
            Logger logger,
            TNotification notification,
            TNetworkManager networkManager,
            TAreaManager areaManager,
            TAreaRenderer areaRenderer,
            ServerSetup serverSetup,
            TClientConfig clientConfig,
            TKeyBindings keyBindings,
            TPlayerState playerState,
            TLevelState levelState,
            TScreenState screenState,
            TCombatState combatState
    ) {
        this.logger = logger;

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

        this.networkManager.init(this);
        this.areaRenderer.init(areaManager, levelState, notification);
        this.keyBindings.init(this);
        this.combatState.init(clientConfig, playerState, levelState, serverSetup);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isFocused();
    protected abstract void disableNativeMusicManager();


    // -----------------------------------------------------------------------------------------------------------------
    // State
    public GameStateProviderV1Real<TBlockPos, TVec3, TBlockState, TEntity> getGameStateProvider() {
        return gameStateProvider;
    }

    public MusicThread getMusicThread() {
        return musicThread;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Music engine
    public void tryReloadMusicEngine()
    {
        if (musicThread != null)
            musicThread.kill();

        combatState.clearCombatants();
        gameStateProvider = new GameStateProviderV1Real<>(
                clientConfig,
                playerState, levelState, screenState, combatState,
                new CaveDetector<>(clientConfig)
        );

        File configFile = Path.of(Common.AMBIENCE_DIRECTORY, Common.MUSIC_CONFIG_FILE).toFile();
        try (InputStream configStream = new FileInputStream(configFile)) {
            Loader.loadFrom(configStream, musicProvider, gameStateProvider).match(
                    this::initMusicThread,
                    this::printErrors
            );
        } catch (IOException ignored) { }
    }

    private void initMusicThread(Interpreter interpreter) {
        disableNativeMusicManager();
        musicThread = new MusicThread(this, interpreter, musicProvider, logger);

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


    // -----------------------------------------------------------------------------------------------------------------
    // Common Handlers
    public void onLoggedIn(AmVersion serverVersion, boolean isOnLocalServer) {
        serverSetup.serverVersion = serverVersion;
        serverSetup.isOnLocalServer = isOnLocalServer;

        if (serverVersion != null && serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
            networkManager.sendToServer(new ModVersionMessage(BuildConfig.APP_VERSION.toString()));

        if (clientConfig.notifyServerSupport.get() && !isOnLocalServer) {
            if (serverVersion == null)
                notification.printToChat(AmLang.MSG_NO_SERVER_SUPPORT);
            else if (serverVersion.isGreaterThanOrEqual(BuildConfig.APP_VERSION))
                notification.printToChat(AmLang.MSG_FULL_SERVER_SUPPORT);
            else if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
                notification.printToChat(AmLang.MSG_PARTIAL_SERVER_SUPPORT);
            else
                notification.printToChat(AmLang.MSG_OUTDATED_VERSION_ON_SERVER);
        }
    }

    public void onLoggedOut() {
        serverSetup.reset();
        combatState.clearCombatants();
    }
}