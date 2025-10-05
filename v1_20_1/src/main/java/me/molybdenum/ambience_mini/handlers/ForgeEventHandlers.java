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

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandlers
{
    // ------------------------------------------------------------------------------------------------
    // Client events
    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void onScreenChanged(final ScreenEvent.Opening event) {
        if (AmbienceMini.onScreenOpened == null)
            return;

        Screen screen = event.getScreen();
        if (screen instanceof ProgressScreen || screen instanceof ConnectScreen || screen instanceof LevelLoadingScreen || screen instanceof ReceivingLevelScreen)
            AmbienceMini.onScreenOpened.accept(Screens.JOINING);
        else if (screen instanceof DisconnectedScreen || screen instanceof DisconnectedRealmsScreen)
            AmbienceMini.onScreenOpened.accept(Screens.DISCONNECTED);
        else if (screen instanceof PauseScreen) {
            IntegratedServer srv = Minecraft.getInstance().getSingleplayerServer();
            if (srv != null && !srv.isPublished())
                AmbienceMini.onScreenOpened.accept(Screens.PAUSE);
        }
        else if (screen instanceof WinScreen)
            AmbienceMini.onScreenOpened.accept(Screens.CREDITS);
        else if (screen instanceof TitleScreen || screen instanceof JoinMultiplayerScreen || screen instanceof DirectJoinServerScreen || screen instanceof SelectWorldScreen || screen instanceof CreateWorldScreen) {
            AmbienceMini.onScreenOpened.accept(Screens.MAIN_MENU);
        }
    }
}
