package me.molybdenum.ambience_mini.engine.client.core.render.areas;

public enum Direction {
    NORTH(true),
    SOUTH(false),
    EAST(false),
    WEST(true),
    UP(false),
    DOWN(true)

    ;

    public final boolean isNegative;

    Direction(boolean isNegative) {
        this.isNegative = isNegative;
    }
}
