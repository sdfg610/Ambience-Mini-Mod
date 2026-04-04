package me.molybdenum.ambience_mini.engine.client.core.render.drawer;

import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;

public interface QuadDrawer {
    void drawQuad(Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4, Color color, int alpha);

    default void drawFace(Cube.Face face, Color color, int alpha) {
        drawQuad(face.p1, face.p2, face.p3, face.p4, color, alpha);
    }

    default void draw2dRectangle(Vector2i from, Vector2i to, Color color, int alpha) {
        drawQuad(
                new Vector3i(from.x(), from.y(), 0),
                new Vector3i(to.x(), from.y(), 0),
                new Vector3i(to.x(), to.y(), 0),
                new Vector3i(from.x(), to.y(), 0),
                color,
                alpha
        );
    }

    default void draw2dQuad(Vector2i p1, Vector2i p2, Vector2i p3, Vector2i p4, Color color, int alpha) {
        drawQuad(
                p1.toVector3iXY(), p2.toVector3iXY(), p3.toVector3iXY(), p4.toVector3iXY(),
                color,
                alpha
        );
    }
}
