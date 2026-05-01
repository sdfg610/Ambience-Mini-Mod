package me.molybdenum.ambience_mini.v1_18_2;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.engine.client.core.flags.FlagCache;
import me.molybdenum.ambience_mini.engine.server.core.command.CommandRegistry;
import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.v1_18_2.client.core.ClientCore;
import me.molybdenum.ambience_mini.v1_18_2.client.core.networking.ClientNetworkManager;
import me.molybdenum.ambience_mini.v1_18_2.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.v1_18_2.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.v1_18_2.client.core.util.Notification;
import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.client.core.locations.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.util.ClientNameCache;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.util.ServerNameCache;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.compatibility.CompatManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.v1_18_2.client.handlers.RenderHandler;
import me.molybdenum.ambience_mini.v1_18_2.network.Networking;
import me.molybdenum.ambience_mini.v1_18_2.client.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.v1_18_2.client.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.v1_18_2.client.core.state.CombatState;
import me.molybdenum.ambience_mini.v1_18_2.client.core.state.ScreenState;
import me.molybdenum.ambience_mini.v1_18_2.client.core.state.LevelState;
import me.molybdenum.ambience_mini.v1_18_2.client.core.state.PlayerState;
import me.molybdenum.ambience_mini.v1_18_2.server.core.ServerCore;
import me.molybdenum.ambience_mini.v1_18_2.server.core.command.CommandNodeFactory;
import me.molybdenum.ambience_mini.v1_18_2.server.core.locations.StructureReader;
import me.molybdenum.ambience_mini.v1_18_2.server.core.networking.ServerNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Common.MOD_ID)
public class AmbienceMini extends BaseAmbienceMini
{
    // Common
    public static final Logger LOGGER = LogUtils.getLogger();

    // Client
    private static ClientConfig clientConfig;
    public static ClientCore clientCore = null;

    // Server
    public static ServerCore serverCore = null;


    public AmbienceMini()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(AmbienceMini::loadComplete);

        Networking.initialize();

        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onRegisterServerCommands);
        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStopping);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig();

            OverlayRegistry.registerOverlayTop("Area Overlay", RenderHandler::renderAreaOverlay);
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

            clientCore = new ClientCore(
                    LOGGER, new ClientNameCache(), new StructureCache(),
                    new Notification(), new ClientNetworkManager(),
                    new ClientAreaManager(), new AreaRenderer(new Drawer()),
                    new FlagCache(),
                    new ServerSetup(), clientConfig, new KeyBindings(),
                    new PlayerState(), new LevelState(), new ScreenState(), new CombatState()
            );

            fireClientCoreInit();
        }
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Common
    /// Called from a Mixin instead of through the event bus since a bug in MC 1.18.2 causes the "MinecraftForge.EVENT_BUS" to shut down somehow...
    public static void onSoundEngineLoaded() {
        if (clientCore != null && !clientCore.isMusicThreadRunning())
            clientCore.tryReloadMusicEngine();
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
                new ServerNameCache(),
                new ServerAreaManager(),
                new StructureReader(event.getServer()),
                new FlagManager(),
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