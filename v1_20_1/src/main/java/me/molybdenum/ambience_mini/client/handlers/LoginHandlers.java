package me.molybdenum.ambience_mini.client.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class LoginHandlers
{
    @SubscribeEvent
    public static void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        AmbienceMini.clientCore.onLoggedIn(
                extractModVersion(event.getConnection()),
                Minecraft.getInstance().isLocalServer(),
                event.getPlayer().getGameProfile().getId().toString(),
                event.getPlayer().getGameProfile().getName()
        );
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        AmbienceMini.clientCore.onLoggedOut();
    }


    private static AmVersion extractModVersion(Connection connection) {
        var data = NetworkHooks.getConnectionData(connection);
        if (data != null) {
            Pair<String, String> modInfo = data.getModData().getOrDefault(Common.MOD_ID, null);
            if (modInfo != null)
                return AmVersion.tryOfString(modInfo.getValue()).orElse(AmVersion.ZERO);
        }
        return AmVersion.ZERO;
    }
}
