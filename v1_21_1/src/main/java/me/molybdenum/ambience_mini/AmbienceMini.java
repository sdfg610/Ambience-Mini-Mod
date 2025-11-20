package me.molybdenum.ambience_mini;

import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.loader.MusicLoader;
import me.molybdenum.ambience_mini.engine.player.AmbienceThread;
import me.molybdenum.ambience_mini.engine.state.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.state.monitors.VolumeMonitor;
import me.molybdenum.ambience_mini.engine.state.providers.GameStateProviderV1;
import me.molybdenum.ambience_mini.network.Networking;
import me.molybdenum.ambience_mini.setup.ClientConfig;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import me.molybdenum.ambience_mini.state.moniotors.CombatMonitor;
import me.molybdenum.ambience_mini.state.moniotors.ScreenMonitor;
import me.molybdenum.ambience_mini.state.readers.LevelReader_1_21;
import me.molybdenum.ambience_mini.state.readers.PlayerReader_1_21;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

import java.util.function.Supplier;

@Mod(Common.MOD_ID)
public class AmbienceMini extends BaseAmbienceMini {
    // Utils
    public static final Logger LOGGER = LogUtils.getLogger();

    // Setup
    public static ClientConfig clientConfig;
    public static KeyBindings keyBindings;

    // State
    public static PlayerReader_1_21 playerReader;
    public static LevelReader_1_21 levelReader;
    public static ScreenMonitor screenMonitor;
    public static CombatMonitor combatMonitor;
    public static CaveDetector<BlockPos, Vec3, BlockState> caveDetector;
    public static GameStateProviderV1<BlockPos, Vec3, BlockState, Entity> gameStateProvider;

    // Music
    public static AmbienceThread ambienceThread;


    public AmbienceMini(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(AmbienceMini::registerPayloads);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig(modContainer);

            screenMonitor = new ScreenMonitor();
            playerReader = new PlayerReader_1_21();
            levelReader = new LevelReader_1_21();

            modEventBus.addListener(AmbienceMini::registerKeybindings);
            modEventBus.addListener(AmbienceMini::loadComplete);

            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }


    /* Common events */
    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        Networking.registerPayloads(event);
    }


    /* Client events */
    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        keyBindings = new KeyBindings(event);
    }

    public static void loadComplete(final FMLLoadCompleteEvent event) {
        combatMonitor = new CombatMonitor(clientConfig, playerReader, levelReader);
        caveDetector = new CaveDetector<>(clientConfig);

        gameStateProvider = new GameStateProviderV1<>(
                clientConfig, playerReader, levelReader, screenMonitor, combatMonitor, caveDetector
        );

        VolumeMonitor.init(
                clientConfig,
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER),
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC)
        );

        tryReload();
    }


    public static void tryReload()
    {
        if (ambienceThread != null)
            ambienceThread.kill();

        combatMonitor.clearCombatants();

        MusicLoader.loadFrom(Common.AMBIENCE_DIRECTORY, LOGGER, gameStateProvider).ifPresent(interpreter -> {
            disableNativeMusicManager();

            Supplier<Boolean> isFocused = Minecraft.getInstance()::isWindowActive;
            ambienceThread = new AmbienceThread(
                    interpreter, LOGGER, isFocused, clientConfig
            );

            LOGGER.info("Successfully loaded Ambience Mini");
        });
    }

    public static void disableNativeMusicManager()
    {
        Minecraft mc = Minecraft.getInstance();
        ObfuscationReflectionHelper.setPrivateValue(
                Minecraft.class, mc, new NilMusicManager(mc), "musicManager"
        );
    }


    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, path);
    }
}
