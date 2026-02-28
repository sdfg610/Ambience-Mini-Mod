package me.molybdenum.ambience_mini.engine.client.core.render;

import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;

public record Vector2i(int x, int y) {
    public Vector3i toVector3iXY() {
        return new Vector3i(x, y, 0);
    }

    public Vector2i offset(int x, int y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    public Vector2i subtract(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }
}
