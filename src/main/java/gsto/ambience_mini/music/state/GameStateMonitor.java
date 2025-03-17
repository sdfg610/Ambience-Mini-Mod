package gsto.ambience_mini.music.state;

import net.minecraft.client.Minecraft;

import java.util.HashSet;

public class GameStateMonitor
{
    private static Minecraft mc = null;

    private static final HashSet<Runnable> eventsChangedHandlers = new HashSet<>();


    public static void init()
    {
        mc = Minecraft.getInstance();
    }


    public static boolean inMainMenu()
    {
        return mc.player == null && mc.level == null;
    }

    public static boolean isJoiningWorld()
    {
        return mc.player == null && mc.level != null;
    }

    public static boolean inGame()
    {
        return mc.player != null && mc.level != null;
    }


}
