package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.setup.AmBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GatherDataEventHandler
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        gen.addProvider(
            true, new AmBlockTagsProvider(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper())
        );
    }
}
