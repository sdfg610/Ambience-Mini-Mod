package gsto.ambience_mini.state;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;

public class MenuStateChecker
{
    public static boolean inMainMenu(Minecraft mc)
    {
        return mc.player == null || mc.level == null;
    }

    public static boolean inGame(Minecraft mc)
    {
        return mc.player != null && mc.level != null;
    }

    public static boolean inPauseMenu(Minecraft mc)
    {
        return mc.screen instanceof PauseScreen;
    }


    public static boolean onSomeJoiningScreen(Minecraft mc)
    {
        return mc.screen instanceof LevelLoadingScreen
                || mc.screen instanceof ConnectScreen
                || mc.screen instanceof ReceivingLevelScreen;
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

}
