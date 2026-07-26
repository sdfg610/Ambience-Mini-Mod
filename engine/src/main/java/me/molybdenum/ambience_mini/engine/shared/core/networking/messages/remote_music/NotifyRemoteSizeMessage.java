package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.remote_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class NotifyRemoteSizeMessage extends AmMessage {
    public int remoteDataByteSize;
    // TODO: Make a hash for safety?


    public NotifyRemoteSizeMessage(int remoteDataByteSize) {
        this.remoteDataByteSize = remoteDataByteSize;
    }

    public NotifyRemoteSizeMessage(AmReader reader) {
        this(reader.readInt());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(remoteDataByteSize);
    }
}
