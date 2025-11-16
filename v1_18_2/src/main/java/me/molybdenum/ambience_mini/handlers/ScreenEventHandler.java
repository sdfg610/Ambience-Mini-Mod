package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.state.monitors.Screens;
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
    // ------------------------------------------------------------------------------------------------
    // Client events
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenOpened(final ScreenOpenEvent event) {
        Screen screen = event.getScreen();
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
