package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class PutFlagMessage extends AmMessage {
    public String id;
    public String value;
    public boolean overwriteIfExists;


    public PutFlagMessage(String id, String value, boolean overwriteIfExists) {
        this.id = id;
        this.value = value;
        this.overwriteIfExists = overwriteIfExists;
    }

    public PutFlagMessage(AmReader reader) {
        this(reader.readString(), reader.readString(), reader.readBoolean());
    }

    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(id);
        writer.writeString(value);
        writer.writeBoolean(overwriteIfExists);
    }
}
