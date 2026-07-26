package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.remote_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class ProvideMusicChunkMessage extends AmMessage {
    public String musicPath;
    public int offset;
    public byte[] data;


    public ProvideMusicChunkMessage(String musicPath, int offset, byte[] data) {
        this.musicPath = musicPath;
        this.offset = offset;
        this.data = data;
    }

    public ProvideMusicChunkMessage(AmReader reader) {
        this(reader.readString(), reader.readInt(), reader.readByteArray());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(musicPath);
        writer.writeInt(offset);
        writer.writeByteArray(data);
    }
}
