package me.molybdenum.ambience_mini.v1_18_2.client.handlers;

import me.molybdenum.ambience_mini.v1_18_2.AmbienceMini;
import me.molybdenum.ambience_mini.v1_18_2.client.core.state.ScreenState;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.client.core.state.Screens;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenOpenEvent;
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
    public static void onScreenOpened(final ScreenOpenEvent event) {
        Screen screen = event.getScreen();
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
