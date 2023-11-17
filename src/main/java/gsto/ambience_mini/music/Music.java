package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class Music
{
    private final Path _filePath;
    public final String musicName;


    public Music(Path filePath)
    {
        _filePath = filePath;
        musicName = filePath.getFileName().toString();
    }

    public InputStream getMusicStream() {
        try {
            return new FileInputStream(_filePath.toFile());
        } catch (FileNotFoundException ex) {
            AmbienceMini.LOGGER.error("File '" + musicName + "' not found. Fix your Ambience config!", ex);
        }
        return null;
    }
}
