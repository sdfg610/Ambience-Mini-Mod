package me.molybdenum.ambience_mini.v1_18_2.server.handlers;

import me.molybdenum.ambience_mini.v1_18_2.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class SavingHandler {
        @SubscribeEvent()
        public static void onTargetChanged(final WorldEvent.Save event)
        {
            if (event.getWorld() instanceof ServerLevel level)
                AmbienceMini.serverCore.areaManager.saveAreasForDimensionIfLoaded(level.dimension().location().toString());
        }
}
