package me.molybdenum.ambience_mini;

import com.mojang.logging.LogUtils;
import me.molybdenum.ambience_mini.core.Core;
import me.molybdenum.ambience_mini.core.util.Notification;
import me.molybdenum.ambience_mini.engine.compatibility.EssentialCompat;
import me.molybdenum.ambience_mini.engine.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.Common;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Common.MOD_ID)
public class AmbienceMini
{
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Core core = null;


    public AmbienceMini(FMLJavaModLoadingContext context)
    {
        IEventBus modBus = context.getModEventBus();

        Networking.initialize();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            core = new Core(LOGGER);

            core.notification = new Notification();
            core.clientConfig = new ClientConfig(context);

            core.playerState = new PlayerState();
            core.levelState = new LevelState();
            core.screenState = new ScreenState();

            modBus.addListener(AmbienceMini::registerKeybindings);
            modBus.addListener(AmbienceMini::loadComplete);
        }
    }


    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        KeyInputEventHandler.keyBindings = core.keyBindings = new KeyBindings(event, core);
    }

    public static void loadComplete(final FMLLoadCompleteEvent event)
    {
        EssentialCompat.isLoaded = ModList.get().isLoaded("essential");

        VolumeState.init(
                core.clientConfig,
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER),
                Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC)
        );

        core.combatState = new CombatState(core.clientConfig, core.playerState, core.levelState, core.serverSetup);
        Networking.combatState = core.combatState;

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
