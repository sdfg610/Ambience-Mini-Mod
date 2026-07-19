package me.molybdenum.ambience_mini.v1_20_1.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobCombatInteractionMessage;
import me.molybdenum.ambience_mini.v1_20_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobTargetMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class ServerCombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event) {
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
            if (event.getSource().isIndirect() && event.getSource().getEntity() instanceof ServerPlayer player && playerNeedsNotification(player) && isCombatableEntity(target, player))
                AmbienceMini.serverNetwork().sendToPlayer(new MobCombatInteractionMessage(target.getId()), player);
        }
    }

    private static boolean playerNeedsNotification(ServerPlayer player) {
        // Only 2.7.1 and above can receive this message. Only 2.7.5 and below needs this message.
        var version = AmbienceMini.serverNetwork().getPlayerModVersion(player);
        return version.isGreaterThanOrEqual(AmVersion.V_2_7_1) && version.isLessThanOrEqual(AmVersion.V_2_7_5);
    }

    private static boolean isCombatableEntity(Entity entity, Player player) {
        return (entity instanceof Monster || entity instanceof NeutralMob)
                && !(entity instanceof TamableAnimal tam && player != null && player.getUUID() == tam.getOwnerUUID())
                && entity.isAlive();
    }
}
