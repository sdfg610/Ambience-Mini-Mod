package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(modid = Common.MODID)
public class CombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event)
    {
        if (!event.isCanceled()) {
            if (event.getOriginalAboutToBeSetTarget() instanceof ServerPlayer player && event.getNewAboutToBeSetTarget() != player)
                PacketDistributor.sendToPlayer(player, new MobTargetUpdatePacket(event.getEntity().getId(), false));

            else if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)
                PacketDistributor.sendToPlayer(player, new MobTargetUpdatePacket(event.getEntity().getId(), true));
        }
    }
}
