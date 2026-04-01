package me.molybdenum.ambience_mini.engine.client.core.render.areas;

import me.molybdenum.ambience_mini.engine.client.core.render.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;


public class Cube
{
    public final Vector3i size;
    public final Vector3i p, pX, pY, pZ, pXY, pXZ, pYZ, pXYZ;
    public final Edge topNorth, topSouth, topEast, topWest,
            bottomNorth, bottomSouth, bottomEast, bottomWest,
            northWest, northEast, southWest, southEast;
    public final Face north, south, east, west, top, bottom;

    public final Edge[] edges;
    public final Face[] faces;


    public Cube(Vector3i from, Vector3i to) {
        var minAndSize = Vector3i.minAndSizeOf(from, to);
        p = minAndSize.left();
        size = minAndSize.right();

        pX = p.offsetX(size.x());
        pY = p.offsetY(size.y());
        pZ = p.offsetZ(size.z());
        pXY = pX.offsetY(size.y());
        pXZ = pX.offsetZ(size.z());
        pYZ = pY.offsetZ(size.z());
        pXYZ = p.add(size);

        topNorth = new Edge(Direction.UP, Direction.NORTH, pY, pXY);
        topSouth = new Edge(Direction.UP, Direction.SOUTH, pXYZ, pYZ);
        topEast = new Edge(Direction.UP, Direction.EAST, pXY, pXYZ);
        topWest = new Edge(Direction.UP, Direction.WEST, pYZ, pY);

        bottomNorth = new Edge(Direction.DOWN, Direction.NORTH, p, pX);
        bottomSouth = new Edge(Direction.DOWN, Direction.SOUTH, pXZ, pZ);
        bottomEast = new Edge(Direction.DOWN, Direction.EAST, pX, pXZ);
        bottomWest = new Edge(Direction.DOWN, Direction.WEST, p, pZ);

        northWest = new Edge(Direction.NORTH, Direction.WEST, p, pY);
        northEast = new Edge(Direction.NORTH, Direction.EAST, pX, pXY);
        southWest = new Edge(Direction.SOUTH, Direction.WEST, pZ, pYZ);
        southEast = new Edge(Direction.SOUTH, Direction.EAST, pXZ, pXYZ);

        north = new Face(Direction.NORTH, p, pX, pXY, pY);
        south = new Face(Direction.SOUTH, pXZ, pZ, pYZ, pXYZ);
        east = new Face(Direction.EAST, pXZ, pX, pXY, pXYZ);
        west = new Face(Direction.WEST, p, pZ, pYZ, pY);
        top = new Face(Direction.UP, pY, pXY, pXYZ, pYZ);
        bottom = new Face(Direction.DOWN, p, pX, pXZ, pZ);

        edges = new Edge[] {
                topNorth, topSouth, topEast, topWest,
                bottomNorth, bottomSouth, bottomEast, bottomWest,
                northWest, northEast, southWest, southEast
        };
        faces = new Face[] { north, south, east, west, top, bottom };
    }

    public Cube(Area area) {
        this(area.fromBlock, area.toBlock);
    }

    public Vector3i getFromBlock() {
        return p;
    }

    public Vector3i getToBlock() {
        return pXYZ.offset(-1,-1,-1);
    }


    @Nullable
    public FaceHitResult getLookingAt(Vector3d camPos, Vector3d camDir) {
        return Arrays.stream(faces)
                .map(face -> face.getRelativeIntersection(camPos, camDir))
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(hit -> hit.distance))
                .orElse(null);
    }

    public boolean canExtendOrContractBy(Direction direction, int offset) {
        return switch (direction) {
            case NORTH, SOUTH -> size.z() + offset > 0;
            case EAST, WEST -> size.x() + offset > 0;
            case UP, DOWN -> size.y() + offset > 0;
        };
    }

    public Cube extendOrContractTo(Direction direction, int offset) {
        if (direction.isNegative)
            return new Cube(p.offset(direction, offset), pXYZ.offset(-1, -1, -1));
        return new Cube(p, pXYZ.offset(-1, -1, -1).offset(direction, offset));
    }


    @SuppressWarnings("ClassCanBeRecord")
    public static class Edge
    {
        public final Direction face1, face2;
        public final Vector3i p1, p2;


        public Edge(Direction face1, Direction face2, Vector3i p1, Vector3i p2) {
            this.face1 = face1;
            this.face2 = face2;
            this.p1 = p1;
            this.p2 = p2;
        }


        public boolean touches(Direction direction) {
            return direction == face1 || direction == face2;
        }
    }

    public static class Face
    {
        public final Direction direction;
        public final Vector3i p1, p2, p3, p4;

        public final Vector3d vec1, vec2, normal;


        public Face(Direction direction, Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4) {
            this.direction = direction;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.p4 = p4;

            this.vec1 = p2.subtract(p1).toVector3d();
            this.vec2 = p4.subtract(p1).toVector3d();
            this.normal = Vector3d.normalOf(vec1, vec2);
        }

        // Thanks to this thread for the intersection computation: https://math.stackexchange.com/a/3412215/913793
        // Modified a bit since we always have P_p = (0,0,0) or p1 = (0,0,0) here.
        @Nullable
        public FaceHitResult getRelativeIntersection(Vector3d camPos, Vector3d camDir) {
            // Plane assumes that p1 is (0,0,0), so offset camera to compensate.
            var scale = camPos.subtract(p1).invert().dot(normal) / camDir.dot(normal);
            if (Double.isNaN(scale) || scale <= 0) // If camera angle is parallel with plane or if intersection is behind player
                return null;

            var trueIntPos = camPos.add(camDir.mult(scale)); // Intersection position
            var intPos = trueIntPos.subtract(p1);  // Intersection position (assuming that p1 is (0,0,0)).
            var vec1_ = vec1.project(intPos);
            var vec2_ = intPos.subtract(vec1_);
            if (vec1.dot(intPos) < 0 || vec2.dot(intPos) < 0) // Check if intersection is between vec1 and vec2 in positive direction.
                return null;

            // Essentially creates a coordinate system on the face with coordinates (s1, s2)
            var s1 = vec1_.length()/vec1.length();
            var s2 = vec2_.length()/vec2.length();
            return s1 >= 0 && s1 <= 1 && s2 >= 0 && s2 <= 1
                    ? new FaceHitResult(this, camPos.subtract(trueIntPos).length(), s1, s2)
                    : null;
        }

        public Face offset(int offset) {
            return new Face(
                    direction,
                    p1.offset(direction, offset),
                    p2.offset(direction, offset),
                    p3.offset(direction, offset),
                    p4.offset(direction, offset)
            );
        }
    }
}
