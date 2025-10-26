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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenEventHandlers
{
    // ------------------------------------------------------------------------------------------------
    // Client events
    @SubscribeEvent
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        Screen screen = event.getScreen();
        if (screen instanceof ProgressScreen || screen instanceof ConnectScreen || screen instanceof LevelLoadingScreen || screen instanceof ReceivingLevelScreen)
            AmbienceMini.screen.memorizedScreen = Screens.JOINING;

        else if (screen instanceof DisconnectedScreen || screen instanceof DisconnectedRealmsScreen)
            AmbienceMini.screen.memorizedScreen = Screens.DISCONNECTED;

        else if (screen instanceof PauseScreen) {
            IntegratedServer srv = Minecraft.getInstance().getSingleplayerServer();
            if (srv != null && !srv.isPublished())
                AmbienceMini.screen.memorizedScreen = Screens.PAUSE;
        }

        else if (screen instanceof WinScreen)
            AmbienceMini.screen.memorizedScreen = Screens.CREDITS;

        else if (screen instanceof TitleScreen || screen instanceof JoinMultiplayerScreen || screen instanceof DirectJoinServerScreen || screen instanceof SelectWorldScreen || screen instanceof CreateWorldScreen)
            AmbienceMini.screen.memorizedScreen = Screens.MAIN_MENU;

        else if (screen instanceof DeathScreen)
            AmbienceMini.screen.memorizedScreen = Screens.DEATH;
    }
}
