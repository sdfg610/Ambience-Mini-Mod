package me.molybdenum.ambience_mini.engine.client.core.render.drawer;

import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;

public interface LineDrawer {
    void drawLine(Vector3i p1, Vector3i p2, Color color, int alpha);

    default void drawEdge(Cube.Edge edge, Color color, int alpha) {
        drawLine(edge.p1, edge.p2, color, alpha);
    }
}
