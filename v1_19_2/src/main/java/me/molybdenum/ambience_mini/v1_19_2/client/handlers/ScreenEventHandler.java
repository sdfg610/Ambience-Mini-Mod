package me.molybdenum.ambience_mini.v1_19_2.client.handlers;

import me.molybdenum.ambience_mini.v1_19_2.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_19_2.client.core.state.ScreenState;
import net.minecraft.client.gui.screens.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenEventHandler
{
    public static ScreenState screenState;


    static {
        AmbienceMini.registerOnClientCoreInitListener(
                () -> screenState = AmbienceMini.clientCore.screenState
        );
    }


    // ------------------------------------------------------------------------------------------------
    // Client events
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenOpened(final ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        screenState.handleScreenChanged(screen == null ? null : screen.getClass());
    }
}
