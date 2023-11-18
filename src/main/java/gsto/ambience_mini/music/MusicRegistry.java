package gsto.ambience_mini.music;

import java.util.HashMap;
import java.util.Map;

public class MusicRegistry
{
    private static final Map<String, Music[]> globalEvents = new HashMap<>();






    public static void clear()
    {
        globalEvents.clear();
    }
}
