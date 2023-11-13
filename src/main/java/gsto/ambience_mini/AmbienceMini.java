package gsto.ambience_mini;

import gsto.ambience_mini.music.MusicLoader;
import gsto.ambience_mini.music.MusicPlayerThread;
import gsto.ambience_mini.music.NilMusicManager;
import gsto.ambience_mini.setup.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraftforge.common.MinecraftForge;
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
    private static final String OBF_MC_MUSIC_MANAGER = "f_91044_";

    public static final String MODID = "ambience_mini";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static MusicPlayerThread musicPlayerThread;


    public AmbienceMini()
    {
        Config.register();

        // Register the setup method for mod-loading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        //MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
    }


    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event)
    {
        tryReload();
    }

    public void tryReload()
    {
        if (musicPlayerThread != null)
            musicPlayerThread.kill();

        if (!Config.enabled.get())
            LOGGER.info("Not enabled in config. Ambience Mini is disabled.");
        else if (MusicLoader.loadConfig())
        {
            Minecraft mc = Minecraft.getInstance();
            MusicManager nilMusicManager = new NilMusicManager(mc);
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, nilMusicManager, OBF_MC_MUSIC_MANAGER);

            musicPlayerThread = new MusicPlayerThread();

            LOGGER.info("Successfully loaded Ambience Mini");
        }
    }
}
