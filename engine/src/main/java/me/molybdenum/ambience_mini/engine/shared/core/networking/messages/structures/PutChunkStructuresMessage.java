package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.core.structures.AmStructure;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutChunkStructuresMessage extends AmMessage {
    public final String dimension;
    public final Map<Vector2i, List<AmStructure>> chunkToStructures;


    public PutChunkStructuresMessage(AmReader reader) {
        this(reader.readString(), readMap(reader));
    }

    public PutChunkStructuresMessage(String dimension, Map<Vector2i, List<AmStructure>> chunkToStructures) {
        this.dimension = dimension;
        this.chunkToStructures = chunkToStructures;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(dimension);
        writeMap(writer);
    }

    private void writeMap(AmWriter writer) {
        writer.writeInt(chunkToStructures.size());
        for (var entry : chunkToStructures.entrySet()) {
            entry.getKey().writeTo(writer);
            writer.writeList(entry.getValue());
        }
    }

    private static Map<Vector2i, List<AmStructure>> readMap(AmReader reader) {
        int size = reader.readInt();
        var chunkToStructures = new HashMap<Vector2i, List<AmStructure>>(size);
        for (int i = 0; i < size; i++)
            chunkToStructures.put(
                    new Vector2i(reader),
                    reader.readList(AmStructure::new)
            );
        return chunkToStructures;
    }
}
