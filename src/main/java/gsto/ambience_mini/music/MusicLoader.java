package gsto.ambience_mini.music;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gsto.ambience_mini.AmbienceMini;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class MusicLoader
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
            AmbienceMini.LOGGER.warn("Could not find config file: '" + configFilePath + "'. Ambience Mini is disabled.");
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
            load(rootElement.getAsJsonObject(), new Stack<>());
        }
        catch (Exception ex) {
            AmbienceMini.LOGGER.error("Could not load config file", ex);
            return false;
        }

        return true;
    }

    private static void load(JsonObject element, Stack<String> triggers)
    {
        for (var child: element.entrySet()) {
            String[] categoryAndValue = child.getKey().replaceAll("\\s+", "").split("\\.", 2);
            if (categoryAndValue.length != 2)
                throw new IllegalStateException("Rules in '" + MUSIC_CONFIG_FILE_NAME +"' must consist of a 'category' and a 'value' separated bu a dot: '[CAT].[VAL]'");
            String value = categoryAndValue[1];
            switch (categoryAndValue[0]) {
                case MusicEvents.CATEGORY_DIMENSION,
                        MusicEvents.CATEGORY_BIOME,
                        MusicEvents.CATEGORY_STRUCTURE,
                        MusicEvents.CATEGORY_BOSS -> { }

                case MusicEvents.CATEGORY_TRIGGER -> {
                    triggers.push(value);
                    load(child.getValue().getAsJsonObject(), triggers);
                    triggers.pop();
                }

                case MusicEvents.CATEGORY_EVENT -> {
                    var musicNames = new ArrayList<String>();
                    child.getValue().getAsJsonArray().iterator().forEachRemaining(elem -> musicNames.add(elem.getAsString()));
                    registerMusicRule(value, musicNames, triggers);
                }
            }
        }
    }

    private static void registerMusicRule(String event, List<String> musicNames, Stack<String> triggers)
    {
        var music = musicNames.stream().map(
                name -> musicCache.computeIfAbsent(name, key -> new Music(getMusicPath(key)))
        ).toList();
        MusicRegistry.addRule(event, new MusicRule(null, null, null, null, new ArrayList<>(triggers), music));
    }

    private static Path getMusicPath(String musicName)
    {
        if (!musicName.endsWith(".mp3"))
            return Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, musicName + ".mp3");
        return Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, musicName);
    }
}
