package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;


@EventBusSubscriber(modid = Common.MODID)
public class CombatClientHandler
{
    @SubscribeEvent
    public static void onGameModeChanged(final ClientPlayerChangeGameTypeEvent event) {
        AmbienceMini.isSurvivalOrAdventureMode = event.getNewGameType().isSurvival();
        if (!AmbienceMini.isSurvivalOrAdventureMode)
            AmbienceMini.combatMonitor.clearCombatants();
    }

    @SubscribeEvent
    public static void onLivingIncomingDamageEvent(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player
                && event.getSource().getEntity() instanceof Mob attacker
                && attacker.isAlive()) {
            AmbienceMini.combatMonitor.tryAddCombatantByRef(attacker, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackEvent(final AttackEntityEvent event) {
        if (event.getTarget() instanceof Mob target
                && target.canAttackType(EntityType.PLAYER)
                && target.isAlive()) {
            AmbienceMini.combatMonitor.tryAddCombatantByRef(target, true);
        }
    }

    @SubscribeEvent
    public static void onDeathEvent(final LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            assert Minecraft.getInstance().player != null;
            if (!Minecraft.getInstance().player.isAlive()) // If on a lan server, another player's death will get here on the host
                AmbienceMini.combatMonitor.clearCombatants();
        }
        else
            AmbienceMini.combatMonitor.removeCombatant(event.getEntity().getId());
    }
}
