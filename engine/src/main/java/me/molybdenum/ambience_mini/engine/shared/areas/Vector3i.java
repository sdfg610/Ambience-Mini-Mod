package me.molybdenum.ambience_mini.engine.shared.areas;

import me.molybdenum.ambience_mini.engine.client.core.render.areas.Direction;
import me.molybdenum.ambience_mini.engine.client.core.render.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;

public record Vector3i(int x, int y, int z) {
    public static final Vector3i ZERO = new Vector3i(0,0,0);
    public static final Vector3i ONE = new Vector3i(1,1,1);

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


    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }


    public static Pair<Vector3i, Vector3i> minAndSizeOf(Vector3i from, Vector3i to) {
        Vector3i min = new Vector3i(
                Math.min(from.x(), to.x()),
                Math.min(from.y(), to.y()),
                Math.min(from.z(), to.z())
        );

        Vector3i size = new Vector3i(
                Math.max(from.x(), to.x()),
                Math.max(from.y(), to.y()),
                Math.max(from.z(), to.z())
        ).offset(1,1,1).subtract(min);

        return new Pair<>(min, size);
    }
}
