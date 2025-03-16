package gsto.ambience_mini.music.player;

import gsto.ambience_mini.AmbienceMini;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Music
{
    private final Path _filePath;
    public final String musicName;
    public final float gain;


    public Music(Path filePath) {
        this(filePath, 0f);
    }

    public Music(Path filePath, float gain)
    {
        _filePath = filePath;
        musicName = filePath.getFileName().toString();
        this.gain = gain;
    }


    public boolean checkFileExists() {
        return Files.exists(_filePath);
    }

    public InputStream getMusicStream() {
        try {
            return new FileInputStream(_filePath.toFile());
        } catch (FileNotFoundException ex) {
            AmbienceMini.LOGGER.error("File '{}' not found. Fix your Ambience config!", musicName, ex);
        }
        return null;
    }

    @Override
    public String toString()
    {
        return musicName + " (" + _filePath + ")";
    }
}
