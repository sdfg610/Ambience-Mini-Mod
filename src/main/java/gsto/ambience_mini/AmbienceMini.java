package gsto.ambience_mini;

import gsto.ambience_mini.music.NilMusicManager;
import gsto.ambience_mini.setup.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AmbienceMini.MODID)
public class AmbienceMini
{
    private static final String OBF_MC_MUSIC_MANAGER = "f_91044_";

    public static final String MODID = "ambience_mini";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static File ambienceDir;


    public AmbienceMini()
    {
        Config.register();

        // Register the setup method for mod-loading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }


    private void clientSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Client setup done");

        Minecraft mc = Minecraft.getInstance();
        MusicManager nilMusicManager = new NilMusicManager(mc);

        ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, nilMusicManager, OBF_MC_MUSIC_MANAGER);

        File configDir = new File(Paths.get("").toAbsolutePath().toString());
        ambienceDir = new File(configDir, "ambience_music");
        if(!ambienceDir.exists())
            ambienceDir.mkdir();
    }

}
