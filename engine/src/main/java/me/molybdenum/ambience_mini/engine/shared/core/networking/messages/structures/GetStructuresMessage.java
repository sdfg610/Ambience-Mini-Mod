package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.List;

public class GetStructuresMessage extends AmMessage {
    // If true, gets for 'the server-player's current chunk' the start-chunk for each structure that touch this chunk.
    // If false, gets the structures that start within 'the server-player's current chunk'.
    public final boolean getReferences;
    public final List<Vector2i> chunksToFetch;


    public GetStructuresMessage(AmReader reader) {
        this(reader.readBoolean(), reader.readList(Vector2i::new));
    }

    public GetStructuresMessage(boolean getReferences, List<Vector2i> chunksToFetch) {
        this.getReferences = getReferences;
        this.chunksToFetch = chunksToFetch;
    }

    @Override
    public void writeTo(AmWriter writer) {
        writer.writeBoolean(getReferences);
        writer.writeList(chunksToFetch);
    }
}
