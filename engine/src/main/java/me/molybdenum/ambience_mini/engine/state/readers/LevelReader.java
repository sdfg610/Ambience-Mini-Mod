package me.molybdenum.ambience_mini.engine.state.readers;

import org.jetbrains.annotations.Nullable;

public interface LevelReader<TBlockPos, TVec3, TBlockState> {
    boolean isNull();
    boolean notNull();

    String getDimensionId();
    String getBiomeID(TBlockPos blockPos);

    int getTime();

    boolean isRaining();
    boolean isThundering();
    boolean isColdEnoughToSnow(TBlockPos blockPos);

    int countNearbyVillagers(TBlockPos center, int horizontalRadius, int verticalRadius);
    int countNearbyAnimals(TBlockPos center, int horizontalRadius, int verticalRadius);

    boolean canSeeSky(TBlockPos blockPos);
    int getMaxSkyLight(TBlockPos blockPos);

    @Nullable
    TBlockPos tryGetNearestBlockInDirection(TVec3 from, TVec3 to);
    TBlockState getBlockState(TBlockPos blockPos);
}
