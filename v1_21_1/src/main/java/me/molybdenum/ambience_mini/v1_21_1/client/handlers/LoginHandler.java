package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class LoginHandler
{
    @SubscribeEvent
    public static void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        AmbienceMini.clientCore.onLoggedIn(
                AmbienceMini.configuredAmVersion,
                Minecraft.getInstance().isLocalServer(),
                event.getPlayer().getGameProfile().getId().toString(),
                event.getPlayer().getGameProfile().getName()
        );
        AmbienceMini.configuredAmVersion = null;
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        AmbienceMini.configuredAmVersion = null;
        AmbienceMini.clientCore.onLoggedOut();
    }
}
