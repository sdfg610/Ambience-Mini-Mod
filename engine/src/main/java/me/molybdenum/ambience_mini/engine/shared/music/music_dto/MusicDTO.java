package me.molybdenum.ambience_mini.engine.shared.music.music_dto;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;

public record MusicDTO(String musicPath, int musicSize, Map<String, ValueDTO> flags) implements AmSerializable
{
    public MusicDTO(Music music, BaseMusicProvider musicProvider) throws FileNotFoundException {
        this(music.path(), musicProvider.getMusicSize(music), music.getMusicFlags());
    }

    public MusicDTO(AmReader reader) {
        this(reader.readString(), reader.readInt(), reader.readStringKeyedMap(ValueDTO::readFrom));
    }


    public int getSerializedLength() {
        return musicPath.length() + Integer.BYTES + Integer.BYTES + flags.entrySet().stream()
                .mapToInt(entry -> entry.getKey().length() + Integer.BYTES + entry.getValue().getSerializedLength())
                .sum();
    }

    public Optional<ValueDTO> tryGetFlag(String flagKey) {
        return Optional.ofNullable(flags.get(flagKey));
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(musicPath);
        writer.writeInt(musicSize);
        writer.writeStringKeyedMap(flags);
    }
}
