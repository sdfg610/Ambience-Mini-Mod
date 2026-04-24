package me.molybdenum.ambience_mini.v1_19_2;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.client.core.locations.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.util.ClientNameCache;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.util.ServerNameCache;
import me.molybdenum.ambience_mini.engine.shared.compatibility.CompatManager;
import me.molybdenum.ambience_mini.v1_19_2.client.core.ClientCore;
import me.molybdenum.ambience_mini.v1_19_2.client.core.networking.ClientNetworkManager;
import me.molybdenum.ambience_mini.v1_19_2.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.v1_19_2.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.v1_19_2.client.core.util.Notification;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.v1_19_2.client.handlers.RenderHandler;
import me.molybdenum.ambience_mini.v1_19_2.network.Networking;
import me.molybdenum.ambience_mini.v1_19_2.client.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.v1_19_2.client.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.v1_19_2.client.core.state.CombatState;
import me.molybdenum.ambience_mini.v1_19_2.client.core.state.ScreenState;
import me.molybdenum.ambience_mini.v1_19_2.client.core.state.LevelState;
import me.molybdenum.ambience_mini.v1_19_2.client.core.state.PlayerState;
import me.molybdenum.ambience_mini.v1_19_2.server.core.ServerCore;
import me.molybdenum.ambience_mini.v1_19_2.server.core.locations.StructureReader;
import me.molybdenum.ambience_mini.v1_19_2.server.core.networking.ServerNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Common.MOD_ID)
public class AmbienceMini extends BaseAmbienceMini
{
    private static final String OBF_SOUND_ENGINE_LOADED = "f_120219_";

    // Common
    public static final Logger LOGGER = LogUtils.getLogger();

    // Client
    private static ClientConfig clientConfig;
    private static KeyBindings keyBindings;
    public static ClientCore clientCore = null;

    // Server
    public static ServerCore serverCore = null;


    public AmbienceMini(FMLJavaModLoadingContext context)
    {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(AmbienceMini::loadComplete);

        Networking.initialize();

        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStopping);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig(context);
            modBus.addListener(AmbienceMini::registerGuiOverlays);
            modBus.addListener(AmbienceMini::registerKeybindings);
            modBus.addListener(AmbienceMini::onSoundEngineLoaded);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, path);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Common
    public static void loadComplete(final FMLLoadCompleteEvent event)
    {
        CompatManager.init(ModList.get()::isLoaded);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            VolumeState.init(
                    clientConfig,
                    Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER),
                    Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC),
                    Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.RECORDS)
            );

            Notification notification = new Notification();
            clientCore = new ClientCore(
                    LOGGER, new ClientNameCache(), new StructureCache(),
                    notification, new ClientNetworkManager(),
                    new ClientAreaManager(), new AreaRenderer(new Drawer()),
                    new ServerSetup(), clientConfig, keyBindings,
                    new PlayerState(), new LevelState(), new ScreenState(), new CombatState()
            );

            fireClientCoreInit();
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Client
    private static void registerGuiOverlays(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("area_overlay", RenderHandler::renderAreaOverlay);
    }

    private static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        keyBindings = new KeyBindings(event);
    }

    private static void onSoundEngineLoaded(final SoundEngineLoadEvent event) {
        if (clientCore != null && !clientCore.isMusicThreadRunning() && Boolean.TRUE.equals(ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, event.getEngine(), OBF_SOUND_ENGINE_LOADED)))
            clientCore.tryReloadMusicEngine();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Server
    private static void onServerStarting(final ServerStartingEvent event) {
        serverCore = new ServerCore(
                event.getServer(),
                LOGGER,
                new ServerNameCache(),
                new ServerAreaManager(),
                new StructureReader(event.getServer()),
                new ServerNetworkManager()
        );
        serverCore.init();
        serverCore.onStarted();
    }

    private static void onServerStopping(final ServerStoppedEvent ignored) {
        if (serverCore != null) {
            serverCore.onStopping();
            serverCore = null;
        }
    }


    public static ServerNetworkManager serverNetwork() {
        return serverCore.networkManager;
    }
}
