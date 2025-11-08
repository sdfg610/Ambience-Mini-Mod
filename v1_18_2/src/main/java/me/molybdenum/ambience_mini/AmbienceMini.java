package me.molybdenum.ambience_mini;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.player.AmbienceThread;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.loader.MusicLoader;
import me.molybdenum.ambience_mini.engine.state.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.state.monitors.VolumeMonitor;
import me.molybdenum.ambience_mini.engine.state.providers.GameStateProviderV1;
import me.molybdenum.ambience_mini.network.Networking;
import me.molybdenum.ambience_mini.setup.ClientConfig;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import me.molybdenum.ambience_mini.state.monitors.CombatMonitor;
import me.molybdenum.ambience_mini.state.monitors.ScreenMonitor;
import me.molybdenum.ambience_mini.state.readers.LevelReader_1_18;
import me.molybdenum.ambience_mini.state.readers.PlayerReader_1_18;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.util.function.Supplier;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Common.MODID)
public class AmbienceMini extends BaseAmbienceMini
{
    // Utils
    public static final String OBF_MC_MUSIC_MANAGER = "f_91044_";

    public static final Logger LOGGER = LogUtils.getLogger();

    // Setup
    public static ClientConfig clientConfig;
    public static KeyBindings keyBindings;

    // Music
    public static PlayerReader_1_18 playerReader;
    public static LevelReader_1_18 levelReader;

    public static ScreenMonitor screenMonitor;
    public static CombatMonitor combatMonitor;
    public static CaveDetector<BlockPos, Vec3, BlockState> caveDetector;

    public static AmbienceThread ambienceThread;


    public AmbienceMini()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(AmbienceMini::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig();

            playerReader = new PlayerReader_1_18();
            levelReader = new LevelReader_1_18();

            screenMonitor = new ScreenMonitor();
            combatMonitor = new CombatMonitor(clientConfig, playerReader, levelReader);

            modBus.addListener(AmbienceMini::loadComplete);
        }
    }


    private static void commonSetup(FMLCommonSetupEvent event)
    {
        Networking.initialize();
    }

    public static void loadComplete(final FMLLoadCompleteEvent event)
    {
        keyBindings = new KeyBindings();
        caveDetector = new CaveDetector<>(clientConfig);

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
        var gameStateProvider = new GameStateProviderV1<>(
                clientConfig, playerReader, levelReader, screenMonitor, combatMonitor, caveDetector
        );

        MusicLoader.loadFrom(Common.AMBIENCE_DIRECTORY, LOGGER, gameStateProvider).ifPresent(rule -> {
            disableNativeMusicManager();

            Supplier<Boolean> isFocused = Minecraft.getInstance()::isWindowActive;
            ambienceThread = new AmbienceThread(
                    rule, LOGGER, isFocused, clientConfig
            );

            LOGGER.info("Successfully loaded Ambience Mini");
        });
    }

    private static void disableNativeMusicManager()
    {
        Minecraft mc = Minecraft.getInstance();
        ObfuscationReflectionHelper.setPrivateValue(
                Minecraft.class, mc, new NilMusicManager(mc), OBF_MC_MUSIC_MANAGER
        );
    }


    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Common.MODID, path);
    }
}