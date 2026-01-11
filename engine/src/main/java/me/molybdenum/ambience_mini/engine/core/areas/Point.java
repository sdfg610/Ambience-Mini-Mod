package me.molybdenum.ambience_mini.engine.core.areas;

public record Point(int x, int y, int z) {
    public Point offset(int x, int y, int z) {
        return new Point(this.x + x, this.y + y, this.z + z);
    }

    public Point offsetX(int x) {
        return new Point(this.x + x, this.y, this.z);
    }

    public Point offsetY(int y) {
        return new Point(this.x, this.y + y, this.z);
    }

    public Point offsetZ(int z) {
        return new Point(this.x, this.y, this.z + z);
    }


    public Point add(Point other) {
        return new Point(x + other.x, y + other.y, z + other.z);
    }

    public Point subtract(Point other) {
        return new Point(x - other.x, y - other.y, z - other.z);
    }
}
