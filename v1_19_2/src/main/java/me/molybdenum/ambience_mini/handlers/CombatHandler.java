package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.Networking;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdateMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class CombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event)
    {
        if (!event.isCanceled()) {
            if (event.getOriginalTarget() instanceof ServerPlayer player && event.getNewTarget() != player)
                sendTargetMessage(event.getEntity().getId(), false, player);

            else if (event.getNewTarget() instanceof ServerPlayer player)
                sendTargetMessage(event.getEntity().getId(), true, player);
        }
    }

    private static void sendTargetMessage(int entityId, boolean isTargetingPlayer, ServerPlayer player) {
        // Only send packet if client has Ambience Mini installed. This lets non-modded clients join a modded server.
        if (Networking.INSTANCE.isRemotePresent(player.connection.connection))
            Networking.sendTo(new MobTargetUpdateMessage(entityId, isTargetingPlayer), player);
    }
}
