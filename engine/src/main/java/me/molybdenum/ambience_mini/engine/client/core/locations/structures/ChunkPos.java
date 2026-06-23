package me.molybdenum.ambience_mini.engine.client.core.locations.structures;

import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public record ChunkPos(int x, int z) {
    public ChunkPos(Vector2i vector) {
        this(vector.x(), vector.y());
    }


    public ChunkPos offset(int x, int z) {
        return new ChunkPos(this.x + x, this.z + z);
    }


    public RegionPos getRegionPos() {
        return new RegionPos(x / 32, z / 32);
    }

    public int getRegionChunkIndex() {
        return 32*Math.abs(x % 32) + Math.abs(z % 32);
    }

    public Vector2i asVector2i() {
        return new Vector2i(x, z);
    }
}
