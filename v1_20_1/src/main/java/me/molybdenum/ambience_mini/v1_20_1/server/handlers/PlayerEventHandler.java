package me.molybdenum.ambience_mini.v1_20_1.server.handlers;

import me.molybdenum.ambience_mini.v1_20_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class PlayerEventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerClone(final PlayerEvent.Clone event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer)
            AmbienceMini.serverCore.networkManager.renewPlayerModVersion(oldPlayer, newPlayer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLoggingOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player)
            AmbienceMini.serverCore.networkManager.removePlayerModVersion(player);
    }
}
