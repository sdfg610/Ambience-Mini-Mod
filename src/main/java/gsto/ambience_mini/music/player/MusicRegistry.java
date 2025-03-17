package gsto.ambience_mini.music.player;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.music.state.GameStateManager;
import gsto.ambience_mini.music.state.MusicEvents;
import gsto.ambience_mini.music.loader.MusicRuleOld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicRegistry
{
    private static final Map<String, ArrayList<MusicRuleOld>> events = new HashMap<>();
    private static final Map<String, List<Music>> bosses = new HashMap<>();

    public static void clear()
    {
        events.clear();
        bosses.clear();
    }


    public static void addEventRule(String event, MusicRuleOld rule) {
        events.computeIfAbsent(event, ignore -> new ArrayList<>()).add(rule);
    }

    public static void addBossMusic(String bossName, List<Music> music) {
        bosses.put(bossName, music);
    }

    public static List<Music> getEventMusic(String event) {
        var rules = events.getOrDefault(event, null);
        if (rules == null)
            return null;

        for (var rule : rules)
        {
            boolean isValid =
                    (rule.dimension() == null || rule.dimension().equals(GameStateManager.getDimensionId()))
                    && triggersFulfilled(rule.triggers());
            if (isValid)
                return rule.music();
        }

        return null;
    }

    public static List<Music> getBossMusic(String bossName) {
        for (var boss : bosses.entrySet())
            if (boss.getKey().equals("*") || bossName.contains(boss.getKey())) // Uses contains here since boss-id/keys are like "entity.minecraft.ender_dragon".
                return boss.getValue();
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
