package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.state.monitors.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Common.MODID, value = Dist.CLIENT)
public class ScreenEventHandlers
{
    // ------------------------------------------------------------------------------------------------
    // Client events
    @SubscribeEvent
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen instanceof ProgressScreen
                || screen instanceof ConnectScreen
                || screen instanceof LevelLoadingScreen
                || screen instanceof ReceivingLevelScreen)
            AmbienceMini.screenMonitor.setMemorizedScreen(Screens.JOINING);

        else if (screen instanceof DisconnectedScreen
                || screen instanceof DisconnectedRealmsScreen)
            AmbienceMini.screenMonitor.setMemorizedScreen(Screens.DISCONNECTED);

        else if (screen instanceof WinScreen)
            AmbienceMini.screenMonitor.setMemorizedScreen(Screens.CREDITS);

        else if (screen instanceof TitleScreen
                || screen instanceof JoinMultiplayerScreen
                || screen instanceof DirectJoinServerScreen
                || screen instanceof SelectWorldScreen
                || screen instanceof CreateWorldScreen)
            AmbienceMini.screenMonitor.setMemorizedScreen(Screens.MAIN_MENU);

        else if (screen instanceof DeathScreen)
            AmbienceMini.screenMonitor.setMemorizedScreen(Screens.DEATH);
    }
}
