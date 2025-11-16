package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.setup.AmBlockTagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Common.MOD_ID)
public class GatherDataEventHandler
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(
                true,
                new AmBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper())
        );
    }
}