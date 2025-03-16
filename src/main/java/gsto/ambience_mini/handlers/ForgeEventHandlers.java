package gsto.ambience_mini.handlers;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.music.state.GameStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class ForgeEventHandlers
{
    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent event)
    {
        if(AmbienceMini.musicMonitor == null)
            return;

        if (event.phase == TickEvent.Phase.END)
        {

        }
    }

    @SubscribeEvent
    public static void onEntitySetAttackTargetEvent(final LivingChangeTargetEvent event) {
        if (event.getNewTarget() != null) {
            //Ambience.attacked = true;
            //attackingTimer = attackFadeTime;
            //EventHandlers.playInstant();
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackEvent(final AttackEntityEvent event) {
        String mobName = event.getTarget().getName().getString().toLowerCase();

        if (event.getTarget() != null) {
            //Ambience.attacked = true;
            //attackingTimer = attackFadeTime;
            //EventHandlers.playInstant();
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(final LivingDeathEvent event) {
        DamageSource source = event.getSource();



        // When Player kills something
        if (event.getSource().getEntity() == Minecraft.getInstance().player) {
            //Ambience.attacked = false;
        }

        // When Player dies
        else if (event.getEntity() == Minecraft.getInstance().player) {
            //Ambience.attacked = false;
        }
    }

    @SubscribeEvent
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        GameStateManager.handleScreen(event.getScreen());
    }
}
