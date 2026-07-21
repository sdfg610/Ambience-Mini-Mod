package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.CombatState;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;


@EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT)
public class ClientCombatHandler
{
    private static Minecraft mc = Minecraft.getInstance();
    private static CombatState combat;


    static {
        AmbienceMini.loadIfInitializedOrRegisterListener(
                AmbienceMini::getClientCore, core -> combat = core.combatState
        );
    }


    @SubscribeEvent
    public static void onDeathEvent(final LivingDeathEvent event) {
        combat.handlePlayerDeath(event.getEntity());
    }
}
