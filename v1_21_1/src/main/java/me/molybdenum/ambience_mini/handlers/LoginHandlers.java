package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdatePacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class LoginHandlers
{
    @SubscribeEvent
    public static void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        AmbienceMini.server().isOnLocalServer = Minecraft.getInstance().isLocalServer();
        AmbienceMini.server().hasServerSupport = event.getPlayer().connection.hasChannel(MobTargetUpdatePacket.TYPE.id());
        if (AmbienceMini.config().notifyServerSupport.get() && !AmbienceMini.server().isOnLocalServer)
            AmbienceMini.notification().showToast(AmbienceMini.server().hasServerSupport ? AmLang.TOAST_HAS_SERVER_SUPPORT : AmLang.TOAST_NO_SERVER_SUPPORT);
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        AmbienceMini.server().isOnLocalServer = false;
        AmbienceMini.server().hasServerSupport = false;

        AmbienceMini.combat().clearCombatants();
    }
}
