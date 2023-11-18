package gsto.ambience_mini.handlers;

import gsto.ambience_mini.AmbienceMini;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandlers
{
    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    {

    }
}
