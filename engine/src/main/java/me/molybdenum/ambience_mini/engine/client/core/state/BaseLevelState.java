package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.compatibility.EssentialCompat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity, TClientLevel>
{
    private final ArrayList<Consumer<String>> levelChangedListeners = new ArrayList<>();

    protected TClientLevel cachedLevel = null;

    // RotX = sideways [-180.0 ; +179.99999]
    // RotY = up/down [-90.0 ; +90.0]
    // (X:0,Y:0) = (south, horizontal)
    public static final double Y_ROT_MIN = -90.0;
    public static final double Y_ROT_SPAN = 180.0;
    public static final double X_ROT_MIN = -180.0;
    public static final double X_ROT_SPAN = 360.0;

    public static final int MAX_LIGHT_LEVEL = 15;


    // -----------------------------------------------------------------------------------------------------------------
    // Execution
    public abstract boolean isNull();
    public abstract boolean notNull();

    public void prepare(@Nullable ArrayList<String> messages) {
        TClientLevel newLevel = getCurrentLevel();
        if (cachedLevel != newLevel && EssentialCompat.isNotFakeWorld(newLevel)) {
            if (messages != null)
                messages.add("Level instance changed from '" + getLevelString(cachedLevel) + "' to '" + getLevelString(newLevel) + "' since last update.");
            cachedLevel = newLevel;
            fireLevelChanged(cachedLevel == null ? null : getDimensionID());
        }
    }

    protected abstract TClientLevel getCurrentLevel();
    protected abstract String getLevelString(TClientLevel level);


    // ------------------------------------------------------------------------------------------------
    // World-info
    public abstract Boolean isWorldTickingPaused();
    public abstract String getDifficulty();

    public abstract String getDimensionID();


    // ------------------------------------------------------------------------------------------------
    // Primary State
    public abstract String getBiomeID(TBlockPos blockPos);
    public abstract List<String> getBiomeTagIDs(TBlockPos blockPos);

    public abstract Integer getTime();

    public abstract Boolean isRaining();
    public abstract Boolean isThundering();
    public abstract Boolean isColdEnoughToSnow(TBlockPos blockPos);

    public abstract TEntity getEntityById(int id);
    public abstract TVec3 getEntityPosition(TEntity entity);

    public abstract Integer countNearbyVillagers(TBlockPos center, int horizontalRadius, int verticalRadius);
    public abstract Integer countNearbyAnimals(TBlockPos center, int horizontalRadius, int verticalRadius);
    public abstract Double shortestDistanceToWarden(TVec3 position, int cubeSearchRadius);

    public abstract TBlockState getBlockState(TBlockPos blockPos);
    public abstract Object getBlock(TBlockState blockState);
    public abstract String getBlockId(TBlockState blockState);
    public abstract Stream<String> getBlockTags(TBlockState blockState);
    public abstract TBlockPos getNearestBlockOrFurthestAir(TVec3 from, TVec3 to);

    public abstract boolean isAir(TBlockState blockState);

    public abstract Integer getMaxSkyLightAt(TBlockPos blockPos);
    public abstract Integer getBlockLightAt(TBlockPos blockPos);


    // ------------------------------------------------------------------------------------------------
    // Utility
    public abstract TBlockPos vectorToBlockPos(TVec3 position);
    public abstract TBlockPos offsetBlockPos(TBlockPos blockPos, int x, int y, int z);
    public abstract TVec3 offsetVector(TVec3 position, double x, double y, double z);
    @Nullable public abstract Vector3i toAmVector3i(@Nullable TBlockPos blockPos);
    @Nullable public abstract Vector3d toAmVector3d(@Nullable TVec3 position);


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
    public double getAverageSkyLightingAround(TBlockPos bPos) {
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
    public double getAverageBlockLightingAround(TBlockPos bPos) {
        List<TBlockPos> airPositions = getSurroundingNonCornerPositions(bPos)
                .filter(this::isAirAt)
                .toList();
        if (airPositions.isEmpty())
            return 0;

        int sum = airPositions.stream()
                .map(this::getBlockLightAt)
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


    // ------------------------------------------------------------------------------------------------
    // Listeners
    public void addLevelChangedListener(Consumer<String> listener) {
        levelChangedListeners.add(listener);
    }

    public void removeLevelChangedListener(Consumer<String> listener) {
        levelChangedListeners.remove(listener);
    }

    protected void fireLevelChanged(String newDimensionId) {
        levelChangedListeners.forEach(
                listener -> listener.accept(newDimensionId)
        );
    }
}
