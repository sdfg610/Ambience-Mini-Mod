package me.molybdenum.ambience_mini.v1_20_1;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.client.core.flags.FlagCache;
import me.molybdenum.ambience_mini.engine.client.core.music.ServerMusicCache;
import me.molybdenum.ambience_mini.engine.server.core.command.CommandRegistry;
import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.server.core.music.ServerMusicManager;
import me.molybdenum.ambience_mini.v1_20_1.client.core.ClientCore;
import me.molybdenum.ambience_mini.v1_20_1.client.core.networking.ClientNetworkManager;
import me.molybdenum.ambience_mini.v1_20_1.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.v1_20_1.client.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.v1_20_1.client.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.v1_20_1.client.core.state.CombatState;
import me.molybdenum.ambience_mini.v1_20_1.client.core.state.LevelState;
import me.molybdenum.ambience_mini.v1_20_1.client.core.state.PlayerState;
import me.molybdenum.ambience_mini.v1_20_1.client.core.state.ScreenState;
import me.molybdenum.ambience_mini.v1_20_1.client.core.util.Notification;
import me.molybdenum.ambience_mini.v1_20_1.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.v1_20_1.client.handlers.RenderHandler;
import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.client.core.locations.areas.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.structures.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.misc.ClientNameCache;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.misc.ServerNameCache;
import me.molybdenum.ambience_mini.engine.shared.compatibility.CompatManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.v1_20_1.network.Networking;
import me.molybdenum.ambience_mini.v1_20_1.server.core.ServerCore;
import me.molybdenum.ambience_mini.v1_20_1.server.core.command.CommandNodeFactory;
import me.molybdenum.ambience_mini.v1_20_1.server.core.locations.StructureReader;
import me.molybdenum.ambience_mini.v1_20_1.server.core.networking.ServerNetworkManager;
import me.molybdenum.ambience_mini.v1_20_1.server.core.setup.ServerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
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
    private static ServerConfig serverConfig;
    public static ServerCore serverCore = null;


    public AmbienceMini(FMLJavaModLoadingContext context)
    {
        Networking.initialize();

        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onRegisterServerCommands);
        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStopped);

        IEventBus modBus = context.getModEventBus();
        modBus.addListener(AmbienceMini::loadComplete);

        serverConfig = new ServerConfig(context);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig(context);
            modBus.addListener(AmbienceMini::registerGuiOverlays);
            modBus.addListener(AmbienceMini::registerKeybindings);
            modBus.addListener(AmbienceMini::onSoundEngineLoaded);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Common
    private static void loadComplete(final FMLLoadCompleteEvent event)
    {
        CompatManager.init(ModList.get()::isLoaded);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            VolumeState.init(
                    Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER),
                    Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC),
                    Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.RECORDS)
            );

            baseCore = clientCore = new ClientCore(
                    LOGGER, new ClientNameCache(), new StructureCache(),
                    new Notification(), new ClientNetworkManager(),
                    new ClientAreaManager(), new AreaRenderer(new Drawer()),
                    new FlagCache(), new ServerMusicCache(),
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
        if (clientCore != null && Boolean.TRUE.equals(ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, event.getEngine(), OBF_SOUND_ENGINE_LOADED)))
            clientCore.onSoundEngineReloaded();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Server
    private static void onRegisterServerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                CommandRegistry.build(new CommandNodeFactory(() -> serverCore))
        );
    }

    private static void onServerStarting(final ServerStartingEvent event) {
        serverCore = new ServerCore(
                event.getServer(),
                LOGGER,
                serverConfig,
                new ServerNameCache(),
                new ServerAreaManager(),
                new StructureReader(event.getServer()),
                new FlagManager(),
                new ServerMusicManager(),
                new ServerNetworkManager()
        );
        serverCore.init();
        serverCore.onStarting();
    }

    private static void onServerStopped(final ServerStoppedEvent ignored) {
        if (serverCore != null) {
            serverCore.onStopped();
            serverCore = null;
        }
    }


    public static ServerNetworkManager serverNetwork() {
        return serverCore.networkManager;
    }
}
