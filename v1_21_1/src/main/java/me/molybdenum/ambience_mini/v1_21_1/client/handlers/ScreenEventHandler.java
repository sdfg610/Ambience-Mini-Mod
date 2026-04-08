package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.client.core.state.Screens;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.ScreenState;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT)
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
    @SubscribeEvent
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen instanceof ProgressScreen
                || screen instanceof ConnectScreen
                || screen instanceof LevelLoadingScreen
                || screen instanceof ReceivingLevelScreen)
            screenState.setMemorizedScreen(Screens.JOINING);

        else if (screen instanceof DisconnectedScreen
                || screen instanceof DisconnectedRealmsScreen)
            screenState.setMemorizedScreen(Screens.DISCONNECTED);

        else if (screen instanceof WinScreen)
            screenState.setMemorizedScreen(Screens.CREDITS);

        else if (screen instanceof TitleScreen
                || screen instanceof JoinMultiplayerScreen
                || screen instanceof DirectJoinServerScreen
                || screen instanceof SelectWorldScreen
                || screen instanceof CreateWorldScreen)
            screenState.setMemorizedScreen(Screens.MAIN_MENU);

        else if (screen instanceof DeathScreen)
            screenState.setMemorizedScreen(Screens.DEATH);
    }
}
