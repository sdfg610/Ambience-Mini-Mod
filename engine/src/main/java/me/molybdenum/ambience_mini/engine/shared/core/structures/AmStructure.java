package me.molybdenum.ambience_mini.engine.shared.core.structures;

import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.util.List;

public class AmStructure implements AmSerializable {
    public final String name;
    public final List<Piece> pieces;


    public AmStructure(AmReader reader) {
        name = reader.readString();
        pieces = reader.readList(Piece::new);
    }

    public AmStructure(String name, List<Piece> pieces) {
        this.name = name;
        this.pieces = pieces;
    }


    public boolean containsPosition(Vector3i position) {
        return pieces.stream().anyMatch(piece -> piece.containsPosition(position));
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(name);
        writer.writeList(pieces);
    }


    public record Piece(Vector3i min, Vector3i max) implements AmSerializable
    {
        public Piece(AmReader reader) {
            this(new Vector3i(reader), new Vector3i(reader));
        }


        public boolean containsPosition(Vector3i position) {
            return position.isInside(min, max);
        }


        public void writeTo(AmWriter writer) {
            min.writeTo(writer);
            max.writeTo(writer);
        }
    }
}
