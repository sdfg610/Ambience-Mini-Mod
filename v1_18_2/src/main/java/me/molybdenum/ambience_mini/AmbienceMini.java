package me.molybdenum.ambience_mini;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.AmbienceThread;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.loader.MusicLoader;
import me.molybdenum.ambience_mini.engine.state.providers.GameStateProviderV1;
import me.molybdenum.ambience_mini.engine.state.monitors.Screens;
import me.molybdenum.ambience_mini.setup.Config;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import me.molybdenum.ambience_mini.setup.NilMusicManager;
import me.molybdenum.ambience_mini.state.detectors.CaveDetector;
import me.molybdenum.ambience_mini.state.monitors.ScreenMonitor;
import me.molybdenum.ambience_mini.state.monitors.VolumeMonitor;
import me.molybdenum.ambience_mini.state.readers.LevelReader_1_18;
import me.molybdenum.ambience_mini.state.readers.PlayerReader_1_18;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.function.Consumer;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Common.MODID)
public class AmbienceMini
{
    public static final String OBF_MC_MUSIC_MANAGER = "f_91044_";
    public static final String AMBIENCE_DIRECTORY = "ambience_music";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static Consumer<Pair<SoundSource, Float>> onVolumeChanged;
    public static Consumer<Screens> onScreenOpened;

    public static final Config config = new Config();
    public static final ScreenMonitor screen = new ScreenMonitor();
    public static final PlayerReader_1_18 player = new PlayerReader_1_18();
    public static final LevelReader_1_18 level = new LevelReader_1_18();
    public static CaveDetector caveDetector;

    public static AmbienceThread ambienceThread;



    public AmbienceMini()
    {
        config.register();
        caveDetector = new CaveDetector(config);
        onScreenOpened = scr -> screen.memorizedScreen = scr;

        // Register the setup method for mod-loading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(AmbienceMini::clientSetup);

        // Register ourselves for server and other game events we are interested in
        //MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        KeyBindings.register();
        tryReload();
    }

    public static void tryReload()
    {
        if (ambienceThread != null)
            ambienceThread.kill();

        if (config.enabled.get()) {
            var gameStateProvider = new GameStateProviderV1<>(
                    config, screen, player, level, caveDetector
            );

            MusicLoader.loadFrom(AMBIENCE_DIRECTORY, LOGGER, gameStateProvider).ifPresent(rule -> {
                disableNativeMusicManager();

                ambienceThread = new AmbienceThread(
                        rule,
                        LOGGER,
                        Minecraft.getInstance()::isWindowActive,
                        new VolumeMonitor(config.ignoreMasterVolume),
                        config
                );

                LOGGER.info("Successfully loaded Ambience Mini");
            });
        }
        else
            LOGGER.info("Not enabled in config. Ambience Mini is disabled.");
    }

    public static void disableNativeMusicManager()
    {
        Minecraft mc = Minecraft.getInstance();
        ObfuscationReflectionHelper.setPrivateValue(
                Minecraft.class, mc, new NilMusicManager(mc), OBF_MC_MUSIC_MANAGER
        );
    }
}