package me.molybdenum.ambience_mini.v1_19_2.server.handlers;

import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_19_2.AmbienceMini;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class SavingHandler {
        @SubscribeEvent()
        public static void onTargetChanged(final LevelEvent.Save event)
        {
            if (event.getLevel() instanceof ServerLevel level)
                AmbienceMini.serverCore.areaManager.saveAreasForDimensionIfLoaded(level.dimension().location().toString());
        }
}
