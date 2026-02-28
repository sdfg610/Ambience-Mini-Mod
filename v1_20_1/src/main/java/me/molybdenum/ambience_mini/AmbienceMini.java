package me.molybdenum.ambience_mini;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.client.core.ClientCore;
import me.molybdenum.ambience_mini.client.core.areas.ClientAreaManager;
import me.molybdenum.ambience_mini.client.core.networking.ClientNetworkManager;
import me.molybdenum.ambience_mini.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.client.core.state.*;
import me.molybdenum.ambience_mini.client.core.util.Notification;
import me.molybdenum.ambience_mini.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.client.handlers.RenderHandler;
import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.compatibility.CompatManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.client.core.state.VolumeState;
import me.molybdenum.ambience_mini.network.Networking;
import me.molybdenum.ambience_mini.client.core.setup.*;
import me.molybdenum.ambience_mini.server.core.ServerCore;
import me.molybdenum.ambience_mini.server.core.managers.ClientManager;
import me.molybdenum.ambience_mini.server.core.managers.ServerNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.*;
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
    private static KeyBindings keyBindings;
    public static ClientCore clientCore = null;

    // Server
    public static ServerCore serverCore = null;


    public AmbienceMini(FMLJavaModLoadingContext context)
    {
        Networking.initialize();

        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(AmbienceMini::onServerStopping);

        IEventBus modBus = context.getModEventBus();
        modBus.addListener(AmbienceMini::loadComplete);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientConfig = new ClientConfig(context);
            modBus.addListener(AmbienceMini::registerGuiOverlays);
            modBus.addListener(AmbienceMini::registerKeybindings);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, path);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Client
    private static void registerGuiOverlays(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("test_overlay", RenderHandler::renderAreaOverlay);
    }

    private static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        keyBindings = new KeyBindings(event);
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
                    LOGGER,
                    notification, new ClientNetworkManager(),
                    new ClientAreaManager(), new AreaRenderer(notification, new Drawer()),
                    new ServerSetup(), clientConfig, keyBindings,
                    new PlayerState(), new LevelState(), new ScreenState(), new CombatState()
            );
            clientCore.tryReloadMusicEngine();

            fireClientCoreInit();
        }
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Server
    private static void onServerStarting(final ServerStartingEvent event) {
        serverCore = new ServerCore(
                LOGGER,
                new ClientManager(),
                new ServerNetworkManager()
        );
    }

    private static void onServerStopping(final ServerStoppedEvent ignored) {
        serverCore.stop();
        serverCore = null;
    }


    public static ClientManager clients() {
        return serverCore.clientManager;
    }

    public static ServerNetworkManager serverNetwork() {
        return serverCore.networkManager;
    }
}
