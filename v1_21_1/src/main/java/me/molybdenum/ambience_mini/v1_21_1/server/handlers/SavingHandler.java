package me.molybdenum.ambience_mini.v1_21_1.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;


@EventBusSubscriber(modid = Common.MOD_ID)
public class SavingHandler {
        @SubscribeEvent()
        public static void onTargetChanged(final LevelEvent.Save event)
        {
            if (event.getLevel() instanceof ServerLevel level)
                AmbienceMini.serverCore.areaManager.saveAreasForDimensionIfLoaded(level.dimension().location().toString());
        }
}
