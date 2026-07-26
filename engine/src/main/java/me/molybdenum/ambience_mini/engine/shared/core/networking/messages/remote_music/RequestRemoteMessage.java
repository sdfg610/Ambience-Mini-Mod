package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.remote_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class RequestRemoteMessage extends AmMessage {
    public RequestRemoteMessage(int maximalRemoteSize) {
        this.maximalRemoteSize = maximalRemoteSize;
    }

    public RequestRemoteMessage(AmReader reader) {
        this(reader.readInt());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(maximalRemoteSize);
    }
}
