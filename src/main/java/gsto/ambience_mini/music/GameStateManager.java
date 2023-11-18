package gsto.ambience_mini.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.LocalPlayer;

import javax.annotation.Nullable;

public class GameStateManager
{
    private static boolean isJoiningWorld = false;
    private static boolean isPaused = false;


    //
    // Menu states
    //

    public static boolean inMainMenu(Minecraft mc)
    {
        return mc.player == null || mc.level == null;
    }

    public static boolean inGame(Minecraft mc)
    {
        return mc.player != null && mc.level != null;
    }

    public static boolean inPauseMenu()
    {
        return isPaused;
    }

    public static boolean possiblyInSoundOptions(Minecraft mc)
    {
        return isPaused || inMainMenu(mc);
    }


    public static boolean isJoiningWorld()
    {
        return isJoiningWorld;
    }

    public static boolean onDisconnectedScreen(Minecraft mc)
    {
        return mc.screen instanceof DisconnectedScreen;
    }

    public static boolean onDeathScreen(Minecraft mc)
    {
        return mc.screen instanceof DeathScreen;
    }

    public static boolean onCreditsScreen(Minecraft mc)
    {
        return mc.screen instanceof WinScreen;
    }


    //
    // Environmental states
    //

    public static boolean inBossFight(Minecraft mc)
    {
        // TODO: DO BETTER
        return mc.gui.getBossOverlay().shouldPlayMusic();
    }


    //
    // Player states
    //

    public static boolean isSleeping(LocalPlayer player)
    {
        return player.isSleeping();
    }

    public static boolean isDead(LocalPlayer player)
    {
        return player.isDeadOrDying();
    }




    //
    // Handle events
    //

    public static void handleScreen(Screen screen)
    {
        if (screen == null)
        {
            isJoiningWorld = false;
            isPaused = false;
        }
        else if (screen instanceof LevelLoadingScreen || screen instanceof ConnectScreen || screen instanceof ReceivingLevelScreen || screen instanceof GenericDirtMessageScreen)
            isJoiningWorld = true;
        else if (screen instanceof PauseScreen)
            isPaused = true;
    }
}
