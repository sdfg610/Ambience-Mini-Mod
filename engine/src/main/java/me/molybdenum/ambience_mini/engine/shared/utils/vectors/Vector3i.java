package me.molybdenum.ambience_mini.engine.shared.utils.vectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.client.core.locations.structures.ChunkPos;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Direction;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.NotNull;

public record Vector3i(int x, int y, int z) {
    public static final Vector3i ZERO = new Vector3i(0,0,0);
    public static final Vector3i ONE = new Vector3i(1,1,1);
    public static final Vector3i MIN = new Vector3i(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);
    public static final Vector3i MAX = new Vector3i(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);


    public Vector3i(AmReader reader) {
        this(reader.readInt(), reader.readInt(), reader.readInt());
    }


    public Vector3i offset(int x, int y, int z) {
        return new Vector3i(this.x + x, this.y + y, this.z + z);
    }

    public Vector3i offsetX(int x) {
        return new Vector3i(this.x + x, this.y, this.z);
    }

    public Vector3i offsetY(int y) {
        return new Vector3i(this.x, this.y + y, this.z);
    }

    public Vector3i offsetZ(int z) {
        return new Vector3i(this.x, this.y, this.z + z);
    }


    public Vector3i offset(Direction direction, int offset) {
        return switch (direction) {
            case NORTH -> offsetZ(-offset);  // North decreases Z. Positive offset towards north gives smaller Z.
            case SOUTH -> offsetZ(offset);   // South increases Z. Positive offset towards south gives bigger Z.
            case EAST ->  offsetX(offset);   // East increases X.
            case WEST ->  offsetX(-offset);  // West decreases X.
            case UP ->    offsetY(offset);   // Up increases Y.
            case DOWN ->  offsetY(-offset);  // Down decreases Y.
        };
    }

    public int getDirectedDistanceTo(Direction direction, Vector3i other) {
        return switch (direction) {
            case NORTH -> z - other.z;
            case SOUTH -> other.z - z;
            case EAST ->  other.x - x;
            case WEST ->  x - other.x;
            case UP ->    other.y - y;
            case DOWN ->  y - other.y;
        };
    }


    public Vector3i add(Vector3i other) {
        return new Vector3i(x + other.x, y + other.y, z + other.z);
    }

    public Vector3i subtract(Vector3i other) {
        return new Vector3i(x - other.x, y - other.y, z - other.z);
    }


    public boolean isInside(Vector3i min, Vector3i max) {
        return x >= min.x() && x <= max.x
                && y >= min.y && y <= max.y
                && z >= min.z && z <= max.z;
    }


    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(x / 16, z / 16);
    }


    public int volume() {
        return x * y * z;
    }

    public static int volume(Vector3i fromBlock, Vector3i toBlock) {
        return sizeOf(fromBlock, toBlock).volume();
    }


    public static @NotNull Pair<Vector3i, Vector3i> minAndSizeOf(Vector3i from, Vector3i to) {
        return minAndMaxOf(from, to).destruct(
                (min, max) -> new Pair<>(min, max.offset(1,1,1).subtract(min))
        );
    }

    public static @NotNull Vector3i sizeOf(Vector3i from, Vector3i to) {
        return minAndMaxOf(from, to).destruct(
                (min, max) -> max.offset(1,1,1).subtract(min)
        );
    }

    public static @NotNull Pair<Vector3i, Vector3i> minAndMaxOf(Vector3i from, Vector3i to) {
        return new Pair<>(minOf(from, to), maxOf(from, to));
    }

    public static @NotNull Vector3i maxOf(Vector3i v1, Vector3i v2) {
        return new Vector3i(
                Math.max(v1.x(), v2.x()),
                Math.max(v1.y(), v2.y()),
                Math.max(v1.z(), v2.z())
        );
    }

    public static @NotNull Vector3i minOf(Vector3i v1, Vector3i v2) {
        return new Vector3i(
                Math.min(v1.x(), v2.x()),
                Math.min(v1.y(), v2.y()),
                Math.min(v1.z(), v2.z())
        );
    }


    public static boolean validateJson(JsonElement elem) {
        if (!Utils.isJsonObjectWith(elem, "x", "y", "z"))
            return false;

        JsonObject obj = elem.getAsJsonObject();
        return Utils.isJsonNumber(obj.get("x"))
                && Utils.isJsonNumber(obj.get("y"))
                && Utils.isJsonNumber(obj.get("z"));
    }

    public static Vector3i fromJson(JsonObject obj) {
        return new Vector3i(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.add("x", new JsonPrimitive(x));
        obj.add("y", new JsonPrimitive(y));
        obj.add("z", new JsonPrimitive(z));
        return obj;
    }


    public void writeTo(AmWriter writer) {
        writer.writeInt(x);
        writer.writeInt(y);
        writer.writeInt(z);
    }
}
