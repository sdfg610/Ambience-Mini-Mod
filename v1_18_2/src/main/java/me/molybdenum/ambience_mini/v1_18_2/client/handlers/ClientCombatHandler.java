package me.molybdenum.ambience_mini.v1_18_2.client.handlers;

import me.molybdenum.ambience_mini.v1_18_2.AmbienceMini;
import me.molybdenum.ambience_mini.v1_18_2.client.core.state.CombatState;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT)
public class ClientCombatHandler
{
    private static final Minecraft mc = Minecraft.getInstance();
    private static CombatState combat;


    static {
        AmbienceMini.registerOnClientCoreInitListener(
                () -> combat = AmbienceMini.clientCore.combatState
        );
    }


    @SubscribeEvent
    public static void onLivingAttackEvent(final LivingAttackEvent event) {
        combat.handleInteraction(event.getEntity(), event.getSource().getEntity());
    }

    @SubscribeEvent
    public static void onDeathEvent(final LivingDeathEvent event) {
        combat.handlePlayerDeath(event.getEntity());
    }
}
