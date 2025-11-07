package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.Networking;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdateMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MODID)
public class CombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event)
    {
        if (!event.isCanceled()) {
            if (event.getOriginalTarget() instanceof ServerPlayer player && event.getNewTarget() != player)
                Networking.sendTo(new MobTargetUpdateMessage(event.getEntity().getId(), false), player);

            else if (event.getNewTarget() instanceof ServerPlayer player)
                Networking.sendTo(new MobTargetUpdateMessage(event.getEntity().getId(), true), player);
        }
    }
}
