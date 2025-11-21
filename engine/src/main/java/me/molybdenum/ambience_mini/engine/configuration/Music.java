package me.molybdenum.ambience_mini.engine.configuration;

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


    public InputStream getMusicStream() throws FileNotFoundException {
        return new FileInputStream(_filePath.toFile());
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


    public boolean isMP3() {
        return musicName.endsWith(".mp3");
    }

    public boolean isFLAC() {
        return musicName.endsWith(".flac");
    }
}
