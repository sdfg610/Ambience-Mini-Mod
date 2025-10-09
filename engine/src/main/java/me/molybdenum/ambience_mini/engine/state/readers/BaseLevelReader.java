package me.molybdenum.ambience_mini.engine.state.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class BaseLevelReader<TBlockPos, TVec3, TBlockState>
{
    // RotX = sideways [-180.0 ; +179.9]
    // RotY = up/down [-90.0 ; +90.0]
    // (X:0,Y:0) = (south, horizontal)
    public static final double Y_ROT_MIN = -90.0;
    public static final double Y_ROT_SPAN = 180.0;
    public static final double X_ROT_MIN = -180.0;
    public static final double X_ROT_SPAN = 360.0;

    public static final int MAX_LIGHT_LEVEL = 15;

    // ------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isNull();
    public abstract boolean notNull();

    public abstract String getDimensionId();
    public abstract String getBiomeID(TBlockPos blockPos);

    public abstract int getTime();

    public abstract boolean isRaining();
    public abstract boolean isThundering();
    public abstract boolean isColdEnoughToSnow(TBlockPos blockPos);

    public abstract int countNearbyVillagers(TBlockPos center, int horizontalRadius, int verticalRadius);
    public abstract int countNearbyAnimals(TBlockPos center, int horizontalRadius, int verticalRadius);

    public abstract TBlockState getBlockState(TBlockPos blockPos);
    public abstract TBlockPos getNearestBlockOrFurthestAir(TVec3 from, TVec3 to);
    public abstract boolean isAir(TBlockState blockState);

    public abstract int getMaxSkyLightAt(TBlockPos blockPos);
    public abstract int getBlockLightAt(TBlockPos blockPos);

    public abstract TBlockPos vectorToBlockPos(TVec3 position);
    public abstract TBlockPos offsetBlockPos(TBlockPos blockPos, int x, int y, int z);
    public abstract TVec3 offsetVector(TVec3 position, double x, double y, double z);


    // ------------------------------------------------------------------------------------------------
    // Helpers
    public boolean isAirAt(TBlockPos blockPos) {
        return isAir(getBlockState(blockPos));
    }

    public TVec3 offsetVectorByAngle(TVec3 position, double xRot, double yRot, double distance) {
        double vecX = Math.sin(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));
        double vecY = Math.sin(-xRot * (Math.PI / 180.0));
        double vecZ = Math.cos(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));

        double xOff = vecX * distance;
        double yOff = vecY * distance;
        double zOff = vecZ * distance;

        return offsetVector(position, xOff, yOff, zOff);
    }

    public TBlockPos getNearestBlockOrFurthestAir(TVec3 fromVec, double xRot, double yRot, double distance) {
        TVec3 toVec = offsetVectorByAngle(fromVec, xRot, yRot, distance);
        return getNearestBlockOrFurthestAir(fromVec, toVec);
    }


    // ------------------------------------------------------------------------------------------------
    // Functions for reading the state of the world around some position
    public List<BlockReading<TBlockPos, TBlockState>> readSurroundings(
            TVec3 origin, int xGranularity, int yGranularity, int radius
    ) {
        List<BlockReading<TBlockPos, TBlockState>> readings = new ArrayList<>(2+xGranularity*yGranularity);

        readings.add(readDirection(origin, 0, 90.0, radius)); // Straight up
        readings.add(readDirection(origin, 0, -90.0, radius)); // Straight down

        for (int yStep = 0; yStep < yGranularity; yStep++) {
            double yRot = Y_ROT_MIN + (((double) (yStep + 1) / (yGranularity + 1)) * Y_ROT_SPAN);
            for (int xStep = 0; xStep < xGranularity; xStep++) {
                double xRot = X_ROT_MIN + (((double) xStep / xGranularity) * X_ROT_SPAN);
                readings.add(readDirection(origin, xRot, yRot, radius));
            }
        }

        return readings;
    }

    public BlockReading<TBlockPos, TBlockState> readDirection(
            TVec3 origin, double xRot, double yRot, int radius
    ) {
        TBlockPos bPos = getNearestBlockOrFurthestAir(origin, xRot, yRot, radius);
        return new BlockReading<>(bPos, getBlockState(bPos), xRot, yRot);
    }


    // ------------------------------------------------------------------------------------------------
    // Lighting-helpers
    public double getAverageLightingAround(TBlockPos bPos) {
        List<TBlockPos> airPositions = getSurroundingNonCornerPositions(bPos)
                .filter(this::isAirAt)
                .toList();
        if (airPositions.isEmpty())
            return 0;

        int sum = airPositions.stream()
                .map(this::getMaxSkyLightAt)
                .reduce(0, Integer::sum);
        return (double) sum / airPositions.size();
    }

    public Stream<TBlockPos> getSurroundingNonCornerPositions(TBlockPos bPos) {
        return Stream.of( // Does not include diagonals
                offsetBlockPos(bPos, 1,0,0),
                offsetBlockPos(bPos, -1,0,0),
                offsetBlockPos(bPos, 0,1,0),
                offsetBlockPos(bPos, 0,-1,0),
                offsetBlockPos(bPos, 0,0,1),
                offsetBlockPos(bPos, 0,0,-1)
        );
    }
}
