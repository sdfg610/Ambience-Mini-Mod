package me.molybdenum.ambience_mini.v1_19_2.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobCombatInteractionMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobTargetMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.v1_19_2.AmbienceMini;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerCombatHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTargetChanged(final LivingChangeTargetEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Mob mob)
            handleTargetChanged(mob.getId(), mob.getTarget(), event.getNewTarget());
    }

    public static void handleTargetChanged(int attackerID, LivingEntity originalTarget, LivingEntity newTarget) {
        if (originalTarget instanceof ServerPlayer player && newTarget != player)
            AmbienceMini.serverNetwork().sendTargetMessage(attackerID, false, player);

        else if (newTarget instanceof ServerPlayer player)
            AmbienceMini.serverNetwork().sendTargetMessage(attackerID, true, player);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttackEvent(final LivingAttackEvent event) {
        if (!event.isCanceled()) {
            var target = event.getEntity();
            var source = event.getSource().getEntity();
            if (event.getSource() instanceof IndirectEntityDamageSource && source instanceof ServerPlayer player)
                sendCombatMobInteractionMessage(target, player);
            else if (target instanceof ServerPlayer player)
                sendCombatMobInteractionMessage(source, player);
        }
    }

    private static void sendCombatMobInteractionMessage(Entity mob , ServerPlayer player) {
        // Only send packet if client has compatible version Ambience Mini installed and if the mob is "combatable".
        if (AmbienceMini.serverNetwork().getPlayerModVersion(player).isGreaterThanOrEqual(AmVersion.V_2_7_1) && isCombatableEntity(mob, player))
            AmbienceMini.serverNetwork().sendToPlayer(new MobCombatInteractionMessage(mob.getId()), player);
    }

    public static boolean isCombatableEntity(Entity entity, Player player) {
        return (entity instanceof Monster || entity instanceof NeutralMob)
                && !(entity instanceof TamableAnimal tam && player != null && player.getUUID() == tam.getOwnerUUID())
                && entity.isAlive();
    }
}
