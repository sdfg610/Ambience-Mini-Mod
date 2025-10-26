package me.molybdenum.ambience_mini;

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
import me.molybdenum.ambience_mini.state.readers.LevelReader_1_21;
import me.molybdenum.ambience_mini.state.readers.PlayerReader_1_21;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

import java.util.function.Supplier;

@Mod(Common.MODID)
public class AmbienceMini {
    // Utils

    public static final Logger LOGGER = LogUtils.getLogger();

    // Setup
    public static final Config config = new Config();
    public static BaseKeyBindings<KeyMapping> keyBindings;

    // Music
    public static final ScreenMonitor screen = new ScreenMonitor();
    public static VolumeMonitor volume;

    public static final PlayerReader_1_21 player = new PlayerReader_1_21();
    public static final LevelReader_1_21 level = new LevelReader_1_21();
    public static CaveDetector<BlockPos, Vec3, BlockState> caveDetector;

    public static AmbienceThread ambienceThread;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AmbienceMini(IEventBus modEventBus, ModContainer modContainer) {
        config.register(modContainer);

        modEventBus.addListener(AmbienceMini::loadComplete);
        modEventBus.addListener(AmbienceMini::registerKeybindings);
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
                Minecraft.class, mc, new NilMusicManager(mc), "musicManager"
        );
    }
}
