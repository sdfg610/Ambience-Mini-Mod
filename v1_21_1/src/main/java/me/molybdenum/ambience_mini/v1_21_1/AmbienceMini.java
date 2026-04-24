package me.molybdenum.ambience_mini.v1_21_1;

import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.client.core.locations.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.util.ClientNameCache;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.util.ServerNameCache;
import me.molybdenum.ambience_mini.engine.shared.compatibility.CompatManager;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.v1_21_1.client.core.ClientCore;
import me.molybdenum.ambience_mini.v1_21_1.client.core.networking.ClientNetworkManager;
import me.molybdenum.ambience_mini.v1_21_1.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.v1_21_1.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.v1_21_1.client.core.util.Notification;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.v1_21_1.client.handlers.RenderHandler;
import me.molybdenum.ambience_mini.v1_21_1.network.Networking;
import me.molybdenum.ambience_mini.v1_21_1.client.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.v1_21_1.client.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.CombatState;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.ScreenState;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.LevelState;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.PlayerState;
import me.molybdenum.ambience_mini.v1_21_1.server.core.ServerCore;
import me.molybdenum.ambience_mini.v1_21_1.server.core.locations.StructureReader;
import me.molybdenum.ambience_mini.v1_21_1.server.core.networking.ServerNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Common.MOD_ID)
public class AmbienceMini extends BaseAmbienceMini
{
    // Common
    public static final Logger LOGGER = LogUtils.getLogger();

    // Client
    private static ClientConfig clientConfig;
    private static KeyBindings keyBindings;
    public static ClientCore clientCore = null;

    public static AmVersion configuredAmVersion = null;

    // Server
    public static ServerCore serverCore = null;


    public AmbienceMini(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(AmbienceMini::registerConfigurationTasks);
        modEventBus.addListener(AmbienceMini::registerPayloads);

        NeoForge.EVENT_BUS.addListener(AmbienceMini::onServerStarting);
        NeoForge.EVENT_BUS.addListener(AmbienceMini::onServerStopping);

        modEventBus.addListener(AmbienceMini::loadComplete);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig(modContainer);
            modEventBus.addListener(AmbienceMini::registerGuiOverlays);
            modEventBus.addListener(AmbienceMini::registerKeybindings);
            modEventBus.addListener(AmbienceMini::onSoundEngineLoaded);

            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, path);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Common
    private static void registerConfigurationTasks(final RegisterConfigurationTasksEvent event) {
        Networking.registerTasks(event);
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        Networking.registerPayloads(event);
    }

    private static void loadComplete(final FMLLoadCompleteEvent event)
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
    private static void registerGuiOverlays(final RegisterGuiLayersEvent event) {
        event.registerAboveAll(rl("area_overlay"), RenderHandler::renderAreaOverlay);
    }

    private static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        keyBindings = new KeyBindings(event);
    }

    private static void onSoundEngineLoaded(final SoundEngineLoadEvent event) {
        if (clientCore != null && !clientCore.isMusicThreadRunning() && Boolean.TRUE.equals(ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, event.getEngine(), "loaded")))
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
