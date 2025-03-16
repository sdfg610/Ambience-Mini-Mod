package gsto.ambience_mini.music.state;

import net.minecraft.client.Minecraft;
import java.util.HashSet;

public class GameMonitor
{
    private static Minecraft mc = null;

    private static final HashSet<Runnable> eventsChangedHandlers = new HashSet<>();


    public static void init()
    {
        mc = Minecraft.getInstance();
    }
}
