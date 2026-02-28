package me.molybdenum.ambience_mini.engine.client.core.render;

import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;

public record Vector3d(double x, double y, double z)
{
    public Vector3d offset(double x, double y, double z) {
        return new Vector3d(this.x + x, this.y + y, this.z + z);
    }

    public Vector3d offsetX(double x) {
        return new Vector3d(this.x + x, this.y, this.z);
    }

    public Vector3d offsetY(double y) {
        return new Vector3d(this.x, this.y + y, this.z);
    }

    public Vector3d offsetZ(double z) {
        return new Vector3d(this.x, this.y, this.z + z);
    }


    public Vector3d invert() {
        return new Vector3d(-x, -y, -z);
    }

    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public Vector3i round() {
        return new Vector3i((int)x, (int)y, (int)z);
    }


    public Vector3d add(Vector3d other) {
        return new Vector3d(x + other.x, y + other.y, z + other.z);
    }

    public Vector3d subtract(Vector3d other) {
        return new Vector3d(x - other.x, y - other.y, z - other.z);
    }

    public Vector3d subtract(Vector3i other) {
        return new Vector3d(x - other.x(), y - other.y(), z - other.z());
    }

    public Vector3d mult(double scale) {
        return new Vector3d(x*scale, y*scale, z*scale);
    }


    public double dot(Vector3d other) {
        return x*other.x + y*other.y + z*other.z;
    }

    public Vector3d project(Vector3d other) {
        return this.mult(other.dot(this) / this.dot(this));
    }


    public static Vector3d normalOf(Vector3d v1, Vector3d v2) {
        return new Vector3d(
                v1.y()*v2.z() - v1.z()*v2.y(),
                v1.z()*v2.x() - v1.x()*v2.z(),
                v1.x()*v2.y() - v1.y()*v2.x()
        );
    }

    public static Vector3d ofRotationAndDistance(double xRot, double yRot, double length) {
        double vecX = Math.sin(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));
        double vecY = Math.sin(-xRot * (Math.PI / 180.0));
        double vecZ = Math.cos(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));

        return new Vector3d(vecX * length, vecY * length, vecZ * length);
    }
}
