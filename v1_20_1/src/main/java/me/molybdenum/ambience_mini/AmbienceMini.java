package me.molybdenum.ambience_mini;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.AmbienceThread;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.loader.MusicLoader;
import me.molybdenum.ambience_mini.engine.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.engine.state.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.state.monitors.VolumeMonitor;
import me.molybdenum.ambience_mini.engine.state.providers.GameStateProviderV1;
import me.molybdenum.ambience_mini.setup.Config;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import me.molybdenum.ambience_mini.state.moniotors.ScreenMonitor;
import me.molybdenum.ambience_mini.state.readers.LevelReader_1_20;
import me.molybdenum.ambience_mini.state.readers.PlayerReader_1_20;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.util.function.Supplier;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Common.MODID)
public class AmbienceMini
{
    // Utils
    public static final String OBF_MC_MUSIC_MANAGER = "f_91044_";

    public static final Logger LOGGER = LogUtils.getLogger();

    // Setup
    public static final Config config = new Config();
    public static BaseKeyBindings<KeyMapping> keyBindings;

    // Music
    public static final ScreenMonitor screen = new ScreenMonitor();
    public static VolumeMonitor volume;

    public static final PlayerReader_1_20 player = new PlayerReader_1_20();
    public static final LevelReader_1_20 level = new LevelReader_1_20();
    public static CaveDetector<BlockPos, Vec3, BlockState> caveDetector;

    public static AmbienceThread ambienceThread;


    public AmbienceMini(FMLJavaModLoadingContext context)
    {
        config.register(context);

        // Register the setup method for mod-loading
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(AmbienceMini::registerKeybindings);
        modBus.addListener(AmbienceMini::loadComplete);
    }

    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        keyBindings = new KeyBindings(event).registerKeys();
    }

    public static void loadComplete(final FMLLoadCompleteEvent event) {
        caveDetector = new CaveDetector<>(config);
        volume = new VolumeMonitor(
                config,
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER),
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC)
        );

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

            MusicLoader.loadFrom(Common.AMBIENCE_DIRECTORY, LOGGER, gameStateProvider).ifPresent(rule -> {
                disableNativeMusicManager();

                Supplier<Boolean> isFocused = Minecraft.getInstance()::isWindowActive;
                ambienceThread = new AmbienceThread(
                        rule, LOGGER, isFocused, volume, config
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
