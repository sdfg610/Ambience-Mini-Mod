package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.structures.AmStructure;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutChunkReferencesMessage extends AmMessage {
    public final String dimension;
    public final Map<Vector2i, List<Vector2i>> chunkToReferences;


    public PutChunkReferencesMessage(AmReader reader) {
        this(reader.readString(), readMap(reader));
    }

    public PutChunkReferencesMessage(String dimension, Map<Vector2i, List<Vector2i>> chunkToReferences) {
        this.dimension = dimension;
        this.chunkToReferences = chunkToReferences;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(dimension);
        writeMap(writer);
    }

    private void writeMap(AmWriter writer) {
        writer.writeInt(chunkToReferences.size());
        for (var entry : chunkToReferences.entrySet()) {
            entry.getKey().writeTo(writer);
            writer.writeList(entry.getValue());
        }
    }

    private static Map<Vector2i, List<Vector2i>> readMap(AmReader reader) {
        int size = reader.readInt();
        var chunkToReferences = new HashMap<Vector2i, List<Vector2i>>(size);
        for (int i = 0; i < size; i++)
            chunkToReferences.put(
                    new Vector2i(reader),
                    reader.readList(Vector2i::new)
            );
        return chunkToReferences;
    }
}
