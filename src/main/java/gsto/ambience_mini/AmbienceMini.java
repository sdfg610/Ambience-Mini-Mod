package gsto.ambience_mini;

import gsto.ambience_mini.music.loader.MusicLoader;
import gsto.ambience_mini.music.state.GameStateMonitor;
import gsto.ambience_mini.music.player.VolumeMonitor;
import gsto.ambience_mini.music.state.GameStateManager;
import gsto.ambience_mini.music.AmbienceThread;
import gsto.ambience_mini.music.assorted.NilMusicManager;
import gsto.ambience_mini.setup.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(AmbienceMini.MODID)
public class AmbienceMini
{
    public static final String AMBIENCE_DIRECTORY = "ambience_music";

    public static final String MODID = "ambience_mini";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String OBF_MC_MUSIC_MANAGER = "f_91044_";
    public static final String OBF_MAP_BOSS_INFO = "f_93699_";

    public static AmbienceThread ambienceThread;


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
        tryReload();
    }

    public static void tryReload() {
        if (ambienceThread != null)
            ambienceThread.kill();

        if (Config.enabled.get()) {
            MusicLoader.loadFrom(AMBIENCE_DIRECTORY).ifPresent(rule -> {
                disableNativeMusicManager();
                GameStateMonitor.init();
                VolumeMonitor.init();

                ambienceThread = new AmbienceThread(rule);

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
