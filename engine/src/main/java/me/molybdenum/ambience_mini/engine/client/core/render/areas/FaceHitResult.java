package me.molybdenum.ambience_mini.engine.client.core.render.areas;

public class FaceHitResult {
    public final Cube.Face face;
    public final double distance;
    public final double scalar1;
    public final double scalar2;


    public FaceHitResult(Cube.Face face, double distance, double scalar1, double scalar2) {
        this.face = face;
        this.distance = distance;
        this.scalar1 = scalar1;
        this.scalar2 = scalar2;
    }
}
