package gsto.ambience_mini.music;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gsto.ambience_mini.AmbienceMini;

import javax.json.Json;
import java.io.*;
import java.nio.file.Path;

public class MusicLoader
{
    // Files and directories
    public static final String AMBIENCE_DIRECTORY_NAME = "ambience_music";
    public static final String MUSIC_DIRECTORY_NAME = "music";
    public static final String MUSIC_CONFIG_FILE_NAME = "music_config.json";

    // Music presets
    public static final Music MUSIC_MAIN_MENU = new Music(Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, "MainMenu.mp3"));
    public static final Music MUSIC_JOINING = new Music(Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, "Joining.mp3"));
    public static final Music MUSIC_BOSS2 = new Music(Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, "Boss2.mp3"));
    public static final Music MUSIC_DEAD = new Music(Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, "Dead.mp3"));
    public static final Music MUSIC_CHILL_DAY1 = new Music(Path.of(AMBIENCE_DIRECTORY_NAME, MUSIC_DIRECTORY_NAME, "ChillDay1.mp3"));


    public static boolean loadConfig()
    {
        Path configFilePath = Path.of(".", AMBIENCE_DIRECTORY_NAME, MUSIC_CONFIG_FILE_NAME);
        File configFile = configFilePath.toFile();
        if (!configFile.exists()) {
            AmbienceMini.LOGGER.warn("Could not find config file: '" + configFilePath + "'. Ambience Mini is disabled.");
            return false;
        }

        try {
            var rootElement = JsonParser.parseReader(new InputStreamReader(new FileInputStream(configFile)));
            if (!rootElement.isJsonObject()) {
                AmbienceMini.LOGGER.error("Root element of config is not a JSON object");
                return false;
            }
            var rootObject = rootElement.getAsJsonObject();

            loadGlobalEvents(rootObject.get(MusicEvents.GLOBAL_GROUP));

        } catch (Exception ex) {
            AmbienceMini.LOGGER.error("Could not load config file", ex);
            return false;
        }

        return true;
    }

    public static void loadGlobalEvents(JsonElement globalElement)
    {

    }
}
