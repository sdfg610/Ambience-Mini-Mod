package me.molybdenum.ambience_mini.v1_21_1.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobCombatInteractionMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobTargetMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;


@EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerCombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Mob mob)
            handleTargetChanged(mob.getId(), mob.getTarget(), event.getNewAboutToBeSetTarget());
    }

    public static void handleTargetChanged(int attackerID, LivingEntity originalTarget, LivingEntity newTarget) {
        if (originalTarget instanceof ServerPlayer player && newTarget != player)
            AmbienceMini.serverNetwork().sendTargetMessage(attackerID, false, player);

        else if (newTarget instanceof ServerPlayer player)
            AmbienceMini.serverNetwork().sendTargetMessage(attackerID, true, player);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttackEvent(final LivingIncomingDamageEvent event) {
        var target = event.getEntity();
        if (!event.getSource().isDirect() && event.getSource().getEntity() instanceof ServerPlayer player && playerNeedsNotification(player) && isCombatableEntity(target, player))
            AmbienceMini.serverCore.networkManager.sendToPlayer(new MobCombatInteractionMessage(target.getId()), player);
    }

    private static boolean playerNeedsNotification(ServerPlayer player) {
        // Only 2.7.1 and above can receive this message. Only 2.7.5 and below needs this message.
        var version = AmbienceMini.serverNetwork().getPlayerModVersion(player);
        return version.isGreaterThanOrEqual(AmVersion.V_2_7_1) && version.isLessThanOrEqual(AmVersion.V_2_7_5);
    }

    private static boolean isCombatableEntity(Entity entity, Entity player) {
        return (entity instanceof Monster || entity instanceof NeutralMob)
                && !(entity instanceof TamableAnimal tam && player != null && player.getUUID() == tam.getOwnerUUID())
                && entity.isAlive();
    }
}
