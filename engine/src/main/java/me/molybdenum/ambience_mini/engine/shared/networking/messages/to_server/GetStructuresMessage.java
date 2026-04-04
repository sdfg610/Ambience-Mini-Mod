package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class GetStructuresMessage extends AmMessage {
    // If true, gets for 'the server-player's current chunk' the start-chunk for each structure that touch this chunk.
    // If false, gets the structures that start within 'the server-player's current chunk'.
    public final boolean getReferences;


    public GetStructuresMessage(AmReader reader) {
        this(reader.readBoolean());
    }

    public GetStructuresMessage(boolean getReferences) {
        this.getReferences = getReferences;
    }

    @Override
    public void writeTo(AmWriter writer) {
        writer.writeBoolean(getReferences);
    }
}
