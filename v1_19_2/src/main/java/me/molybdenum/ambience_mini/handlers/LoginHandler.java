package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.ToastUtil;
import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.Networking;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MODID, value={Dist.CLIENT})
public class LoginHandler
{
    @SubscribeEvent
    public static void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        var gameMode = Minecraft.getInstance().gameMode;
        if (gameMode != null)
            AmbienceMini.isSurvivalOrAdventureMode = gameMode.getPlayerMode().isSurvival();
        AmbienceMini.isOnLocalServer = Minecraft.getInstance().isLocalServer();
        AmbienceMini.hasServerSupport = Networking.INSTANCE.isRemotePresent(event.getConnection());

        if (AmbienceMini.clientConfig.notifyServerSupport.get() && !AmbienceMini.isOnLocalServer)
            ToastUtil.translatable(AmbienceMini.hasServerSupport ? AmLang.TOAST_HAS_SERVER_SUPPORT : AmLang.TOAST_NO_SERVER_SUPPORT);
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        AmbienceMini.isSurvivalOrAdventureMode = false;
        AmbienceMini.isOnLocalServer = false;
        AmbienceMini.hasServerSupport = false;

        AmbienceMini.combatMonitor.clearCombatants();
    }
}
