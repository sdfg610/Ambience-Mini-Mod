package gsto.ambience_mini.music.player;

import gsto.ambience_mini.AmbienceMini;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

public class Music
{
    private final Path _filePath;
    public final String musicName;
    public final float gain;


    public Music(Path filePath, float gain)
    {
        _filePath = filePath;
        musicName = filePath.getFileName().toString();
        this.gain = gain;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return Float.compare(gain, music.gain) == 0 && Objects.equals(_filePath, music._filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_filePath, gain);
    }
}
