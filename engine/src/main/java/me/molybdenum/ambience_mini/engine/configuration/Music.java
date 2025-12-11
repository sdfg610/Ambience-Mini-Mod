package me.molybdenum.ambience_mini.engine.configuration;

import me.molybdenum.ambience_mini.engine.utils.Utils;

import java.util.Objects;

public record Music(String musicPath, float gain)
{
    @Override
    public String toString() {
        return musicPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return Float.compare(gain, music.gain) == 0 && Objects.equals(musicPath, music.musicPath);
    }


    public boolean isMP3() {
        return "mp3".equals(Utils.getFileExtension(musicPath));
    }

    public boolean isFLAC() {
        return "flac".equals(Utils.getFileExtension(musicPath));
    }
}
