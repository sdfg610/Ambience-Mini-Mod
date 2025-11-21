package me.molybdenum.ambience_mini;

import me.molybdenum.ambience_mini.core.Core;
import me.molybdenum.ambience_mini.core.util.Notification;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.core.state.VolumeState;
import me.molybdenum.ambience_mini.handlers.KeyInputEventHandler;
import me.molybdenum.ambience_mini.network.Networking;
import me.molybdenum.ambience_mini.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.core.state.CombatState;
import me.molybdenum.ambience_mini.core.state.ScreenState;
import me.molybdenum.ambience_mini.core.state.LevelState;
import me.molybdenum.ambience_mini.core.state.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Common.MOD_ID)
public class AmbienceMini {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Core core = null;


    public AmbienceMini(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(AmbienceMini::registerPayloads);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            core = new Core(LOGGER);

            core.notification = new Notification();
            core.clientConfig = new ClientConfig(modContainer);

            core.playerState = new PlayerState();
            core.levelState = new LevelState();
            core.screenState = new ScreenState();

            modEventBus.addListener(AmbienceMini::registerKeybindings);
            modEventBus.addListener(AmbienceMini::loadComplete);

            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }


    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        Networking.registerPayloads(event);
    }

    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        KeyInputEventHandler.keyBindings = core.keyBindings = new KeyBindings(event, core);
    }

    public static void loadComplete(final FMLLoadCompleteEvent event) {
        Networking.combatState = core.combatState = new CombatState(core.clientConfig, core.playerState, core.levelState, core.serverSetup);

        VolumeState.init(
                core.clientConfig,
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER),
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC)
        );

        core.tryReload();
    }


    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Common.MOD_ID, path);
    }

    public static Notification notification() {
        return core.notification;
    }

    public static ClientConfig config() {
        return core.clientConfig;
    }

    public static ServerSetup server() {
        return core.serverSetup;
    }

    public static PlayerState player() {
        return core.playerState;
    }

    public static ScreenState screen() {
        return core.screenState;
    }

    public static CombatState combat() {
        return core.combatState;
    }
}
