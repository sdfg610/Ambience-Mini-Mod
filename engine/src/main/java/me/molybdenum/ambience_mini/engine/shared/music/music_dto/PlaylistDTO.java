package me.molybdenum.ambience_mini.engine.shared.music.music_dto;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.util.List;

public record PlaylistDTO(String key, List<MusicDTO> music) implements AmSerializable
{
    public PlaylistDTO(AmReader reader) {
        this(reader.readString(), reader.readList(MusicDTO::new));
    }


    public int getSerializedLength() {
        return key.length() + Integer.BYTES + music.stream().mapToInt(MusicDTO::getSerializedLength).sum();
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(key);
        writer.writeList(music);
    }
}
