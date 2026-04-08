package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.CombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;


@EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT)
public class CombatClientHandler
{
    private static CombatState combat;


    static {
        AmbienceMini.registerOnClientCoreInitListener(
                () -> combat = AmbienceMini.clientCore.combatState
        );
    }


    @SubscribeEvent
    public static void onGameModeChanged(final ClientPlayerChangeGameTypeEvent event) {
        if (!event.getNewGameType().isSurvival())
            combat.clearCombatants();
    }

    @SubscribeEvent
    public static void onLivingIncomingDamageEvent(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player
                && event.getSource().getEntity() instanceof Mob attacker
                && attacker.isAlive()) {
            combat.tryAddCombatantByRef(attacker, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackEvent(final AttackEntityEvent event) {
        if (event.getTarget() instanceof Mob target
                && target.canAttackType(EntityType.PLAYER)
                && target.isAlive()) {
            combat.tryAddCombatantByRef(target, true);
        }
    }

    @SubscribeEvent
    public static void onDeathEvent(final LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            assert Minecraft.getInstance().player != null;
            if (!Minecraft.getInstance().player.isAlive()) // If on a lan server, another player's death will get here on the host
                combat.clearCombatants();
        }
        else
            combat.removeCombatant(event.getEntity().getId());
    }
}
