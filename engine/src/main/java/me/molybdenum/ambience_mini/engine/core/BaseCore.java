package me.molybdenum.ambience_mini.engine.core;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.configuration.Loader;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.FileMusicProvider;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.core.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.core.providers.GameStateProviderV1;
import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.core.state.BaseCombatState;
import me.molybdenum.ambience_mini.engine.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.core.state.BaseScreenState;
import me.molybdenum.ambience_mini.engine.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.music.MusicThread;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;

public abstract class BaseCore<
        TBlockPos, TVec3, TBlockState, TEntity, TKeyBinding, TComponent,
        TNotification extends BaseNotification<TComponent>,
        TClientConfig extends BaseClientConfig,
        TKeyBindings extends BaseKeyBindings<TKeyBinding>,
        TPlayerState extends BasePlayerState<TBlockPos, TVec3>,
        TLevelState extends BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity>,
        TScreenState extends BaseScreenState,
        TCombatState extends BaseCombatState<TEntity, TVec3>
        >
{
    // Utils
    public final Logger logger;
    public TNotification notification;

    // Setup
    public final ServerSetup serverSetup = new ServerSetup();
    public TClientConfig clientConfig;
    public TKeyBindings keyBindings;

    // State
    public TPlayerState playerState;
    public TLevelState levelState;
    public TScreenState screenState;
    public TCombatState combatState;

    private CaveDetector<TBlockPos, TVec3, TBlockState> caveDetector;
    private GameStateProviderV1<TBlockPos, TVec3, TBlockState, TEntity> gameStateProvider;

    // Music
    private MusicThread musicThread;


    public BaseCore(Logger logger) {
        this.logger = logger;
    }


    public void tryReload()
    {
        if (musicThread != null)
            musicThread.kill();

        combatState.clearCombatants();

        caveDetector = new CaveDetector<>(clientConfig);
        gameStateProvider = new GameStateProviderV1<>(
                clientConfig,
                playerState, levelState, screenState, combatState,
                caveDetector
        );


        File configFile = Path.of(Common.AMBIENCE_DIRECTORY, Common.MUSIC_CONFIG_FILE).toFile();
        try (InputStream configStream = new FileInputStream(configFile))
        {
            MusicProvider musicProvider = new FileMusicProvider(Path.of(Common.AMBIENCE_DIRECTORY, Common.MUSIC_DIRECTORY).toString());
            Loader.loadFrom(configStream, musicProvider, gameStateProvider, logger).ifPresent(interpreter -> {
                disableNativeMusicManager();
                musicThread = new MusicThread(this, interpreter, musicProvider, logger);

                if (clientConfig.verboseMode.get())
                    logger.info("Successfully loaded Ambience Mini with configuration:\n{}", clientConfig.getConfigsString());
                else
                    logger.info("Successfully loaded Ambience Mini");
            });
        } catch (IOException ignored) { }
    }


    public CaveDetector<TBlockPos, TVec3, TBlockState> getCaveDetector() {
        return caveDetector;
    }

    public GameStateProviderV1<TBlockPos, TVec3, TBlockState, TEntity> getGameStateProvider() {
        return gameStateProvider;
    }

    public MusicThread getMusicThread() {
        return musicThread;
    }


    public abstract boolean isFocused();

    protected abstract void disableNativeMusicManager();
}
