package gsto.ambience_mini.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;

public class GameStateManager
{
    private static Minecraft mc = null;
    private static boolean isJoiningWorld = false;
    private static boolean isPaused = false;


    public static void init()
    {
        mc = Minecraft.getInstance();
    }


    //
    // Menu states
    //
    public static boolean inMainMenu()
    {
        return mc.player == null || mc.level == null;
    }

    public static boolean inGame()
    {
        return mc.player != null && mc.level != null;
    }

    public static boolean inPauseMenu()
    {
        return isPaused;
    }

    public static boolean possiblyInSoundOptions()
    {
        return isPaused || inMainMenu();
    }


    public static boolean isJoiningWorld()
    {
        return isJoiningWorld;
    }

    public static boolean onDisconnectedScreen()
    {
        return mc.screen instanceof DisconnectedScreen;
    }

    public static boolean onCreditsScreen()
    {
        return mc.screen instanceof WinScreen;
    }


    //
    // Environmental states
    //
    public static boolean isNight()
    {
        assert mc.level != null;
        assert mc.player != null;

        long time = mc.level.getDayTime() % 24000;
        return time > 13200 && time < 23200;
    }

    public static boolean isDownfall()
    {
        assert mc.level != null;
        assert mc.player != null;
        return mc.level.isRaining();
    }


    //
    // Player states
    //
    public static boolean isSleeping()
    {
        assert mc.player != null;
        return mc.player.isSleeping();
    }

    public static boolean isDead()
    {
        assert mc.player != null;
        return mc.player.isDeadOrDying();
    }

    public static boolean inBossFight()
    {
        // TODO: DO BETTER
        return mc.gui.getBossOverlay().shouldPlayMusic();
    }


    //
    // Handle events
    //

    public static void handleScreen(Screen screen)
    {
        if (screen == null || screen instanceof TitleScreen)
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
