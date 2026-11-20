package me.molybdenum.ambience_mini.engine.shared.core.networking.DTOs.music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.DTOs.ValueDTO;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.shared.core.music.music_provider.BaseMusicProvider;

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


    public Music toMusic(boolean locatedOnServer) {
        var gain = tryGetFlag(Music.ARG_GAIN).map(ValueDTO::tryGetFloat).orElse(0f);
        boolean loop = tryGetFlag(Music.ARG_LOOP).map(ValueDTO::tryGetBoolean).orElse(false);
        int loopStart = tryGetFlag(Music.ARG_LOOPSTART).map(ValueDTO::tryGetInt).orElse(-1);
        int loopEnd = tryGetFlag(Music.ARG_LOOPEND).map(ValueDTO::tryGetInt).orElse(-1);
        int loopLength = tryGetFlag(Music.ARG_LOOPLENGTH).map(ValueDTO::tryGetInt).orElse(-1);

        return new Music(
                musicPath,
                locatedOnServer,
                gain,
                loop, loopStart, loopEnd, loopLength
        );
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(musicPath);
        writer.writeInt(musicSize);
        writer.writeStringKeyedMap(flags);
    }
}
