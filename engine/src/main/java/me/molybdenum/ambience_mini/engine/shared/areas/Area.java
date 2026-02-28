package me.molybdenum.ambience_mini.engine.shared.areas;

import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Maybe;

public class Area implements AmSerializable {
    public int id;
    public String name;
    public String dimension;
    public Maybe<Owner> owner; // None -> Public, anyone can do anything ; Has value -> Owned, other players might be able to see, but not edit.

    public Vector3i fromBlock;
    public Vector3i toBlock;

    // Save location (world or local)


    public Area() {
        this(null);
    }

    public Area(String dimension) {
        this(Integer.MIN_VALUE, "New area", dimension, null, null, Maybe.none());
    }

    public Area(int id, String name, String dimension, Vector3i fromBlock, Vector3i toBlock, Maybe<Owner> owner) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
        this.owner = owner;

        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }


    @Override
    public void writeTo(AmWriter writer) {
        // TODO: FILL OUT!!!!!!
    }

    @Override
    public void readFrom(AmReader reader) {
        // TODO: FILL OUT!!!!!!
    }
}
