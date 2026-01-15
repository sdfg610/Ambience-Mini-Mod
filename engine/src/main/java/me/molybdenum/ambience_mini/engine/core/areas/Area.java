package me.molybdenum.ambience_mini.engine.core.areas;

import java.util.UUID;

public class Area {
    public int id;
    public String name;
    public String dimension;
    public Vector3 fromBlock;
    public Vector3 toBlock;

    public UUID owner;
    public boolean isShared = false;


    public Area(int id, String name, String dimension, Vector3 fromBlock, Vector3 toBlock, UUID owner, boolean isShared) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
        this.owner = owner;
        this.isShared = isShared;
    }


    /**
     * @return A vector containing the point with the lowest x, y, and z values within the bounds of the area.
     */
    public Vector3 getMinimumPoint() {
        return new Vector3(
                Math.min(fromBlock.x(), toBlock.x()),
                Math.min(fromBlock.y(), toBlock.y()),
                Math.min(fromBlock.z(), toBlock.z())
        );
    }

    /**
     * @return A vector containing the length (in blocks) of the area in the x, y, and, z directions.
     */
    public Vector3 getSize() {
        Vector3 max = new Vector3(
                Math.max(fromBlock.x(), toBlock.x()),
                Math.max(fromBlock.y(), toBlock.y()),
                Math.max(fromBlock.z(), toBlock.z())
        );
        return max.offset(1,1,1).subtract(getMinimumPoint());
    }
}
