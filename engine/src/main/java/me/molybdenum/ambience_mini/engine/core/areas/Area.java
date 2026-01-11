package me.molybdenum.ambience_mini.engine.core.areas;

public class Area {
    public Point fromBlock;
    public Point toBlock;

    public Area(Point fromBlock, Point toBlock) {
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }


    public Point getMinimumPoint() {
        return new Point(
                Math.min(fromBlock.x(), toBlock.x()),
                Math.min(fromBlock.y(), toBlock.y()),
                Math.min(fromBlock.z(), toBlock.z())
        );
    }

    public Point getSize() {
        Point max = new Point(
                Math.max(fromBlock.x(), toBlock.x()),
                Math.max(fromBlock.y(), toBlock.y()),
                Math.max(fromBlock.z(), toBlock.z())
        );
        return max.offset(1,1,1).subtract(getMinimumPoint());
    }
}
