package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class ModEventHandlers
{
    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    {

    }
}
