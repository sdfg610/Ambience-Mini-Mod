package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.setup.AmBlockTagsProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GatherDataEventHandler
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(
                new AmBlockTagsProvider(generator, event.getExistingFileHelper())
        );
    }
}
