package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.ScreenState;
import net.minecraft.client.gui.screens.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT)
public class ScreenEventHandler
{
    public static volatile ScreenState screenState;


    static {
        AmbienceMini.loadIfInitializedOrRegisterListener(
                AmbienceMini::getClientCore, core -> screenState = core.screenState
        );
    }


    // ------------------------------------------------------------------------------------------------
    // Client events
    @SubscribeEvent
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        ScreenState state = screenState;
        if (state == null) return;   // client core not init, continue

        Screen screen = event.getNewScreen();
        state.handleScreenChanged(screen == null ? null : screen.getClass());
    }
}
