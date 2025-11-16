package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(modid = Common.MOD_ID)
public class CombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event)
    {
        if (!event.isCanceled()) {
            if (event.getOriginalAboutToBeSetTarget() instanceof ServerPlayer player && event.getNewAboutToBeSetTarget() != player)
                sendTargetMessage(player, event, false);

            else if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)
                sendTargetMessage(player, event, true);
        }
    }

    private static void sendTargetMessage(ServerPlayer player, LivingChangeTargetEvent event, boolean isTargetingPlayer) {
        // Only send packet if client has Ambience Mini installed. This lets non-modded clients join a modded server.
        if (player.connection.hasChannel(MobTargetUpdatePacket.TYPE.id()))
            PacketDistributor.sendToPlayer(player, new MobTargetUpdatePacket(event.getEntity().getId(), isTargetingPlayer));
    }
}
