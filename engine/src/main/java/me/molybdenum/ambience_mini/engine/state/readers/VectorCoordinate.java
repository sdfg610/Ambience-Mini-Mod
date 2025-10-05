package me.molybdenum.ambience_mini.engine.state.readers;

public record VectorCoordinate(double x, double y, double z)
{
    public double distanceTo(VectorCoordinate coordinate) {
        double diffX = x - coordinate.x;
        double diffY = y - coordinate.y;
        double diffZ = z - coordinate.z;

        return Math.sqrt(diffX*diffX + diffY*diffY + diffZ*diffZ);
    }

    public VectorCoordinate offset(double x, double y, double z) {
        return new VectorCoordinate(x() + x, y() + y, z + z());
    }
}
