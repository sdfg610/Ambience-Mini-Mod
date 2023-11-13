package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;

import javax.annotation.Nullable;
import javax.json.Json;
import java.io.*;
import java.nio.file.Path;

public class MusicLoader
{
    public static final String ambienceDirectoryName = "ambience_music";
    public static final String musicDirectoryName = "music";
    public static final String musicConfigFileName = "music_config.json";


    public static boolean loadConfig()
    {
        Path configFilePath = Path.of(".", ambienceDirectoryName, musicConfigFileName);
        File configFile = configFilePath.toFile();
        if (!configFile.exists()) {
            AmbienceMini.LOGGER.warn("Could not find config file: '" + configFilePath + "'. Ambience Mini is disabled.");
            return false;
        }

        try {
            var parser = Json.createParser(new FileInputStream(configFile));
        } catch (Exception ex) {
            AmbienceMini.LOGGER.error("Could not load config file", ex);
            return false;
        }

        return true;
    }

    public static InputStream getMusicStream(@Nullable String musicName) {
        if(AmbienceMini.musicPlayerThread == null || musicName == null /* || PlayerThread.currentSong == null || PlayerThread.currentSong.equals("null")*/ )
            return null;

        Path musicPath = Path.of(ambienceDirectoryName, musicDirectoryName, musicName);
        try {
            return new FileInputStream(musicPath.toFile());
        } catch (FileNotFoundException ex) {
            AmbienceMini.LOGGER.error("File '" + musicName + "' not found. Fix your Ambience config!", ex);
        }
        return null;
    }
}
