package gsto.ambience_mini.handlers;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.music.state.GameStateMonitor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandlers
{
    //
    // Client events
    //
    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        GameStateMonitor.handleScreen(event.getScreen());
    }

    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void onDimensionChanged(final PlayerEvent.PlayerChangedDimensionEvent event) {
        // TODO: Pause music while changing dimensions to avoid small hiccup when entering directly into a village or boss fight.
    }



    //
    // Server events
    //
    @SubscribeEvent
    @OnlyIn(value = Dist.DEDICATED_SERVER)
    public static void onEntitySetAttackTargetEvent(final LivingChangeTargetEvent event) {

    }

    @SubscribeEvent
    @OnlyIn(value = Dist.DEDICATED_SERVER)
    public static void onPlayerAttackEvent(final AttackEntityEvent event) {

    }

    @SubscribeEvent
    @OnlyIn(value = Dist.DEDICATED_SERVER)
    public static void onEntityDeath(final LivingDeathEvent event) {

    }
}
