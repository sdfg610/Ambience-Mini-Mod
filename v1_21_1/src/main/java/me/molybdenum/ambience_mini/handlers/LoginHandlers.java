package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.ToastUtil;
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
        var gameMode = Minecraft.getInstance().gameMode;
        if (gameMode != null)
            AmbienceMini.isSurvivalOrAdventureMode = gameMode.getPlayerMode().isSurvival();
        AmbienceMini.isOnLocalServer = Minecraft.getInstance().isLocalServer();

        AmbienceMini.hasServerSupport = event.getPlayer().connection.hasChannel(MobTargetUpdatePacket.TYPE.id());
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
