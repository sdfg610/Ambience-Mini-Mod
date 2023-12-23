package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicRegistry
{
    private static final Map<String, ArrayList<MusicRule>> events = new HashMap<>();

    public static void clear()
    {
        events.clear();
    }


    public static void addRule(String event, MusicRule rule) {
        events.computeIfAbsent(event, ignore -> new ArrayList<>()).add(rule);
    }

    public static List<Music> getMusic(String event) {
        var rules = events.getOrDefault(event, null);
        if (rules == null)
            return null;

        for (var rule : rules)
        {
            boolean isValid = triggersFulfilled(rule.triggers);
            if (isValid)
                return rule.music;
        }

        return null;
    }

    private static boolean triggersFulfilled(List<String> triggers)
    {
        for (var trigger : triggers)
            switch (trigger)
            {
                case MusicEvents.TRIGGER_DAY -> {
                    if (GameStateManager.isNight())
                        return false;
                }

                case MusicEvents.TRIGGER_NIGHT -> {
                    if (!GameStateManager.isNight())
                        return false;
                }

                case MusicEvents.TRIGGER_DOWNFALL -> {
                    if (!GameStateManager.isDownfall())
                        return false;
                }

                default -> AmbienceMini.LOGGER.warn("Could not handle trigger '" + trigger + "'");
            }
        return true;
    }
}
