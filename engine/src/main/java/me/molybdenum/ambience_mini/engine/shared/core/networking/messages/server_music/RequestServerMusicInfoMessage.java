package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class RequestServerMusicInfoMessage extends AmMessage {
    public final String path;


    public RequestServerMusicInfoMessage(String path) {
        this.path = path;
    }

    public RequestServerMusicInfoMessage(AmReader reader) {
        this(reader.readString());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(path);
    }
}
