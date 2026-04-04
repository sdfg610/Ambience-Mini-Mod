package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.List;

public class PutChunkReferenceMessage extends AmMessage {
    public final String dimension;
    public final Vector2i chunkPos;
    public final List<Vector2i> startChunks;


    public PutChunkReferenceMessage(AmReader reader) {
        this(reader.readString(), new Vector2i(reader), reader.readList(Vector2i::new));
    }

    public PutChunkReferenceMessage(String dimension, Vector2i chunkPos, List<Vector2i> startChunks) {
        this.dimension = dimension;
        this.chunkPos = chunkPos;
        this.startChunks = startChunks;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(dimension);
        chunkPos.writeTo(writer);
        writer.writeList(startChunks);
    }
}
