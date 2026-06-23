package me.molybdenum.ambience_mini.engine.shared.core.structures;

import me.molybdenum.ambience_mini.engine.client.core.locations.structures.RegionPos;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AmStructure implements AmSerializable {
    public final String name;
    public final List<Piece> pieces;

    private Piece boundingBox = null;  // The smallest box which contains all pieces.


    public AmStructure(AmReader reader) {
        name = reader.readString();
        pieces = reader.readList(Piece::new);
    }

    public AmStructure(String name, List<Piece> pieces) {
        this.name = name;
        this.pieces = pieces;
    }


    public Piece getBoundingBox() {
        if (boundingBox == null) {
            Vector3i min = Vector3i.MAX, max = Vector3i.MIN;
            for (var piece : pieces) {
                min = Vector3i.minOf(min, piece.min);
                max = Vector3i.maxOf(max, piece.max);
            }
            boundingBox = new Piece(min, max);
        }
        return boundingBox;
    }

    public boolean couldPossiblyContainPosition(Vector3i position) {
        return getBoundingBox().containsPosition(position);
    }

    public boolean containsPosition(Vector3i position) {
        return couldPossiblyContainPosition(position)
                && pieces.stream().anyMatch(piece -> piece.containsPosition(position));
    }

    public Set<RegionPos> getIntersectingRegions() {
        var boundingBox = getBoundingBox();

        HashSet<RegionPos> intersections = new HashSet<>();
        intersections.add(new RegionPos(boundingBox.min.x() / (32*16), boundingBox.min.z() / (32*16))); // Region side length = 32*16
        intersections.add(new RegionPos(boundingBox.min.x() / (32*16), boundingBox.max.z() / (32*16)));
        intersections.add(new RegionPos(boundingBox.max.x() / (32*16), boundingBox.min.z() / (32*16)));
        intersections.add(new RegionPos(boundingBox.max.x() / (32*16), boundingBox.max.z() / (32*16)));
        return intersections;
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
