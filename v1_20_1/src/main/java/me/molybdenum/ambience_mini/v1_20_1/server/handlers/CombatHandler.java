package me.molybdenum.ambience_mini.v1_20_1.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.IndirectAttackOnMobMessage;
import me.molybdenum.ambience_mini.v1_20_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobTargetMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.v1_20_1.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
        // Only send packet if client has compatible version Ambience Mini installed.
        if (AmbienceMini.serverNetwork().getPlayerModVersion(player).isGreaterThanOrEqual(AmVersion.V_2_5_0))
            AmbienceMini.serverNetwork().sendToPlayer(new MobTargetMessage(entityId, isTargetingPlayer), player);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttackEvent(final LivingAttackEvent event) {
        if (!event.isCanceled()) {
            var target = event.getEntity();
            if (event.getSource().isIndirect() && event.getSource().getEntity() instanceof ServerPlayer player && Utils.isCombatableEntity(target))
                sendIndirectAttackOnMobMessage(target.getId(), player);
        }
    }

    private static void sendIndirectAttackOnMobMessage(int entityId, ServerPlayer player) {
        // Only send packet if client has compatible version Ambience Mini installed.
        if (AmbienceMini.serverNetwork().getPlayerModVersion(player).isGreaterThanOrEqual(AmVersion.V_2_7_1)) {
            AmbienceMini.serverNetwork().sendToPlayer(new IndirectAttackOnMobMessage(entityId), player);
        }
    }
}
