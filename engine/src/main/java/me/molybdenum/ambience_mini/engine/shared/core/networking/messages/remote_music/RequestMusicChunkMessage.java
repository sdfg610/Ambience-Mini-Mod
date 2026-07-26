package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.remote_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class RequestMusicChunkMessage extends AmMessage
{
    public String musicPath;
    public int offset;
    public int length;


    public RequestMusicChunkMessage(String musicPath, int offset, int length) {
        this.musicPath = musicPath;
        this.offset = offset;
        this.length = length;
    }

    public RequestMusicChunkMessage(AmReader reader) {
        this(reader.readString(), reader.readInt(), reader.readInt());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(musicPath);
        writer.writeInt(offset);
        writer.writeInt(length);
    }
}
