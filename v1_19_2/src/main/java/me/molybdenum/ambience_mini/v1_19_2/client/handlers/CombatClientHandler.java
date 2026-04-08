package me.molybdenum.ambience_mini.v1_19_2.client.handlers;

import me.molybdenum.ambience_mini.v1_19_2.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_19_2.client.core.state.CombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT)
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
    public static void onLivingAttackEvent(final LivingAttackEvent event) {
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
