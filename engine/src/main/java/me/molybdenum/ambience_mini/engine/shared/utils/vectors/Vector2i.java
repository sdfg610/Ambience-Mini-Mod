package me.molybdenum.ambience_mini.engine.shared.utils.vectors;

import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public record Vector2i(int x, int y) implements AmSerializable
{
    public Vector2i(AmReader reader) {
        this(reader.readInt(), reader.readInt());
    }

    public Vector3i toVector3iXY() {
        return new Vector3i(x, y, 0);
    }

    public Vector2i offset(int x, int y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    public Vector2i subtract(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }

    public Vector2i toRegionPos() {
        return new Vector2i(x / 32, y / 32);
    }

    public Vector2i toRegionChunkPos() {
        return new Vector2i(Math.abs(x % 32), Math.abs(y % 32));
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(x);
        writer.writeInt(y);
    }
}
