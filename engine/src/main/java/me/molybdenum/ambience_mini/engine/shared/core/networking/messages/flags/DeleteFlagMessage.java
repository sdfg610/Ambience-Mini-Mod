package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class DeleteFlagMessage extends AmMessage {
    public String id;


    public DeleteFlagMessage(String id) {
        this.id = id;
    }

    public DeleteFlagMessage(AmReader reader) {
        this(reader.readString());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(id);
    }
}
