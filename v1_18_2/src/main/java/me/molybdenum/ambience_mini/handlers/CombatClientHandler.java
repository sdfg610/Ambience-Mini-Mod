package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
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


@Mod.EventBusSubscriber(modid = Common.MODID, value = Dist.CLIENT)
public class CombatClientHandler
{
    @SubscribeEvent
    public static void onGameModeChanged(final ClientPlayerChangeGameTypeEvent event) {
        AmbienceMini.isSurvivalOrAdventureMode = event.getNewGameType().isSurvival();
        if (!AmbienceMini.isSurvivalOrAdventureMode)
            AmbienceMini.combatMonitor.clearCombatants();
    }

    @SubscribeEvent
    public static void onLivingAttackEvent(final LivingAttackEvent event) {
        if (event.getEntity() instanceof Player
                && event.getSource() instanceof EntityDamageSource source
                && source.getEntity().isAlive()) {
            AmbienceMini.combatMonitor.tryAddCombatantByRef(source.getEntity(), true);
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
