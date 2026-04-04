package me.molybdenum.ambience_mini.engine.client.core.render.drawer;

import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public interface TextDrawer {
    void drawText(String text, Vector2i position, Color color, int alpha);
}
