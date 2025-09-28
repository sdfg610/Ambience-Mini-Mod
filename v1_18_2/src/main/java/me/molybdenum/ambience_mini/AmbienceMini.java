package me.molybdenum.ambience_mini;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.AmbienceThread;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.loader.MusicLoader;
import me.molybdenum.ambience_mini.setup.Config;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import me.molybdenum.ambience_mini.setup.NilMusicManager;
import me.molybdenum.ambience_mini.state.GameStateProvider;
import me.molybdenum.ambience_mini.state.VolumeMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    public static AmbienceThread ambienceThread;
    public static Consumer<Pair<SoundSource, Float>> onVolumeChanged = null;


    public AmbienceMini()
    {
        Config.register();

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

        if (Config.enabled.get()) {
            MusicLoader.loadFrom(AMBIENCE_DIRECTORY, LOGGER, new GameStateProvider()).ifPresent(rule -> {
                disableNativeMusicManager();

                ambienceThread = new AmbienceThread(
                        rule,
                        LOGGER,
                        Config.lostFocusEnabled,
                        Minecraft.getInstance()::isWindowActive,
                        new VolumeMonitor(Config.ignoreMasterVolume)
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