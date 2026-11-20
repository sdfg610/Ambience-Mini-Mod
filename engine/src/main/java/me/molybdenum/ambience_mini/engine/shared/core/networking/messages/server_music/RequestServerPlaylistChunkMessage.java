package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class RequestServerPlaylistChunkMessage extends AmMessage
{
    public int offset;
    public int byteLength;


    public RequestServerPlaylistChunkMessage(int offset, int byteLength) {
        this.offset = offset;
        this.byteLength = byteLength;
    }

    public RequestServerPlaylistChunkMessage(AmReader reader) {
        this(reader.readInt(), reader.readInt());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(offset);
        writer.writeInt(byteLength);
    }
}
