package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.Networking;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class LoginHandler
{
    @SubscribeEvent
    public static void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        AmbienceMini.server().isOnLocalServer = Minecraft.getInstance().isLocalServer();
        AmbienceMini.server().hasServerSupport = Networking.INSTANCE.isRemotePresent(event.getConnection());

        if (AmbienceMini.config().notifyServerSupport.get() && !AmbienceMini.server().isOnLocalServer)
            AmbienceMini.notification().printToChat(AmbienceMini.server().hasServerSupport ? AmLang.TOAST_HAS_SERVER_SUPPORT : AmLang.TOAST_NO_SERVER_SUPPORT);
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        AmbienceMini.server().isOnLocalServer = false;
        AmbienceMini.server().hasServerSupport = false;

        AmbienceMini.combat().clearCombatants();
    }
}
