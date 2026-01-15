package me.molybdenum.ambience_mini.engine.core.areas;

public record Vector3(int x, int y, int z) {
    public Vector3 offset(int x, int y, int z) {
        return new Vector3(this.x + x, this.y + y, this.z + z);
    }

    public Vector3 offsetX(int x) {
        return new Vector3(this.x + x, this.y, this.z);
    }

    public Vector3 offsetY(int y) {
        return new Vector3(this.x, this.y + y, this.z);
    }

    public Vector3 offsetZ(int z) {
        return new Vector3(this.x, this.y, this.z + z);
    }


    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }
}
