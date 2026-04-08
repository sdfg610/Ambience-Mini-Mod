package me.molybdenum.ambience_mini.v1_21_1.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


@EventBusSubscriber(modid = Common.MOD_ID)
public class PlayerEventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerClone(final PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer)
            AmbienceMini.serverCore.networkManager.renewPlayerModVersion(oldPlayer, newPlayer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLoggingOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            AmbienceMini.serverCore.networkManager.removePlayerModVersion(player);
    }
}
