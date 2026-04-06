package me.molybdenum.ambience_mini.engine.shared.utils.vectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Direction;
import me.molybdenum.ambience_mini.engine.shared.areas.Owner;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;

public record Vector3i(int x, int y, int z) {
    public static final Vector3i ZERO = new Vector3i(0,0,0);
    public static final Vector3i ONE = new Vector3i(1,1,1);


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

    public Vector2i toChunkPos() {
        return new Vector2i(x / 16, z / 16);
    }


    public int volume() {
        return x * y * z;
    }

    public static int volume(Vector3i fromBlock, Vector3i toBlock) {
        return sizeOf(fromBlock, toBlock).volume();
    }


    public static Pair<Vector3i, Vector3i> minAndSizeOf(Vector3i from, Vector3i to) {
        return minAndMaxOf(from, to).destruct(
                (min, max) -> new Pair<>(min, max.offset(1,1,1).subtract(min))
        );
    }

    public static Vector3i sizeOf(Vector3i from, Vector3i to) {
        return minAndMaxOf(from, to).destruct(
                (min, max) -> max.offset(1,1,1).subtract(min)
        );
    }

    public static Pair<Vector3i, Vector3i> minAndMaxOf(Vector3i from, Vector3i to) {
        Vector3i min = new Vector3i(
                Math.min(from.x(), to.x()),
                Math.min(from.y(), to.y()),
                Math.min(from.z(), to.z())
        );

        Vector3i max = new Vector3i(
                Math.max(from.x(), to.x()),
                Math.max(from.y(), to.y()),
                Math.max(from.z(), to.z())
        );

        return new Pair<>(min, max);
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
