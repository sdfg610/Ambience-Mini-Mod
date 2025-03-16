package gsto.ambience_mini.music.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.music.player.Music;
import gsto.ambience_mini.music.state.MusicEvents;
import gsto.ambience_mini.music.player.MusicRegistry;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class MusicLoaderOld
{
    // Files and directories
    public static final String AMBIENCE_DIRECTORY_NAME = "ambience_music";
    public static final String MUSIC_DIRECTORY_NAME = "music";
    public static final String MUSIC_CONFIG_FILE_NAME = "music_config.json";

    private static final Map<String, Music> musicCache = new HashMap<>();


    public static boolean loadConfig()
    {
        Path configFilePath = Path.of(".", AMBIENCE_DIRECTORY_NAME, MUSIC_CONFIG_FILE_NAME);
        File configFile = configFilePath.toFile();
        if (!configFile.exists()) {
            AmbienceMini.LOGGER.warn("Could not find config file: '{}'. Ambience Mini is disabled.", configFilePath);
            return false;
        }

        musicCache.clear();
        MusicRegistry.clear();

        try {
            var rootElement = JsonParser.parseReader(new InputStreamReader(new FileInputStream(configFile)));
            if (!rootElement.isJsonObject()) {
                AmbienceMini.LOGGER.error("Root element of config is not a JSON object");
                return false;
            }
            load(rootElement.getAsJsonObject(), null, new Stack<>());
        }
        catch (Exception ex) {
            AmbienceMini.LOGGER.error("Could not load config file", ex);
            return false;
        }

        return true;
    }

    private static void load(JsonObject element, String dimension, Stack<String> triggers)
    {
        for (var child: element.entrySet()) {
            String[] categoryAndValue = child.getKey().replaceAll("\\s+", "").split("\\.", 2);
            if (categoryAndValue.length != 2)
                throw new IllegalStateException("Rules in '" + MUSIC_CONFIG_FILE_NAME +"' must consist of a 'category' and a 'value' separated bu a dot: '[CAT].[VAL]'");

            String value = categoryAndValue[1];
            switch (categoryAndValue[0]) {
                case MusicEvents.CATEGORY_DIMENSION -> {
                    if (dimension != null)
                        throw new IllegalStateException("Config file cannot contain a dimension rule within a dimension rule");
                    load(child.getValue().getAsJsonObject(), ensureStartsWithModId(value), triggers);
                }

                case MusicEvents.CATEGORY_BIOME, MusicEvents.CATEGORY_STRUCTURE -> { }

                case MusicEvents.CATEGORY_TRIGGER -> {
                    triggers.push(value);
                    load(child.getValue().getAsJsonObject(), dimension, triggers);
                    triggers.pop();
                }

                case MusicEvents.CATEGORY_EVENT ->
                    MusicRegistry.addEventRule(
                            value,
                            new MusicRule(dimension, null, null, new ArrayList<>(triggers), getMusicFromNames(getMusicNames(child.getValue())))
                    );

                case MusicEvents.CATEGORY_BOSS -> {
                    if (dimension != null || !triggers.isEmpty())
                        throw new IllegalStateException("'boss'-rules must show up in the root of the config file");

                    MusicRegistry.addBossMusic(value, getMusicFromNames(getMusicNames(child.getValue())));
                }
            }
        }
    }

    private static String ensureStartsWithModId(String value)
    {
        if (!value.contains(":"))
            return "minecraft:" + value;
        return value;
    }

    @Nullable
    private static List<String> getMusicNames(JsonElement musicList)
    {
        var musicNames = new ArrayList<String>();
        if (musicList.isJsonArray()) {
            musicList.getAsJsonArray().iterator().forEachRemaining(elem -> musicNames.add(elem.getAsString()));
            return musicNames;
        }
        else if (musicList.isJsonNull())
            return null;
        throw new IllegalStateException("The value of an 'event'-rule must be an array of strings or 'null'");
    }

    @Nullable
    private static List<Music> getMusicFromNames(@Nullable List<String> musicNames)
    {
        if (musicNames == null)
            return null;

        return musicNames.stream().map(
            name -> musicCache.computeIfAbsent(name, key -> new Music(getMusicPath(key)))
        ).toList();
    }

    private static Path getMusicPath(String musicName)
    {
        if (!musicName.endsWith(".mp3"))
            return Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, musicName + ".mp3");
        return Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, musicName);
    }
}
