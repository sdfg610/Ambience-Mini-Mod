package me.molybdenum.ambience_mini.engine.state.detectors;

import me.molybdenum.ambience_mini.engine.BaseConfig;
import me.molybdenum.ambience_mini.engine.state.readers.LevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.PlayerReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class BaseCaveDetector<TBlockPos, TVec3, TBlockState, TScore>
{
    // RotX = sideways [-180.0 ; +179.9]
    // RotY = up/down [-90.0 ; +90.0]
    // (X:0,Y:0) = (south, horizontal)
    protected static final double Y_ROT_MIN = -90.0;
    protected static final double Y_ROT_SPAN = 180.0;
    protected static final double X_ROT_MIN = -180.0;
    protected static final double X_ROT_SPAN = 360.0;

    protected static final int MAX_LIGHT_LEVEL = 15;


    private final int _caveScoreRadius;
    private final int _measureDistance;
    private final int _xGranularity;
    private final int _yGranularity;


    protected BaseCaveDetector(BaseConfig config)
    {
        _caveScoreRadius = config.caveScoreRadius.get();
        _measureDistance = config.caveMeasureDistance.get();
        _xGranularity = config.xAxisGranularity.get();
        _yGranularity = config.yAxisGranularity.get();
    }


    public Optional<Double> getAveragedCaveScore(LevelReader<TBlockPos, TVec3, TBlockState> level, PlayerReader<TBlockPos, TVec3> player)
    {
        List<Double> scores = new ArrayList<>();
        for (int xOff = -_caveScoreRadius; xOff <= _caveScoreRadius; xOff++)
            for (int yOff = -_caveScoreRadius; yOff <= _caveScoreRadius; yOff++)
                for (int zOff = -_caveScoreRadius; zOff <= _caveScoreRadius; zOff++) {
                    TVec3 vOrigin = offsetV(player.eyePosition(), xOff, yOff, zOff);
                    TBlockPos bOrigin = offsetB(player.eyeBlockPos(), xOff, yOff, zOff);
                    if (isAir(level.getBlockState(bOrigin)))
                        scores.add(getCaveScore(level, vOrigin, bOrigin));
                }

        if (scores.isEmpty())
            return Optional.empty();

        return Optional.of(scores.stream().reduce(0.0, Double::sum) / scores.size());
    }

    private double getCaveScore(LevelReader<TBlockPos, TVec3, TBlockState> level, TVec3 vOrigin, TBlockPos bOrigin)
    {
        List<TScore> scores = new ArrayList<>();
        scores.add(testDirection(level, vOrigin, 0, 90.0)); // Straight up
        scores.add(testDirection(level, vOrigin, 0, -90.0)); // Straight down

        for (int yStep = 0; yStep < _yGranularity; yStep++) {
            double yRot = Y_ROT_MIN + (((double) (yStep + 1) / (_yGranularity + 1)) * Y_ROT_SPAN);
            for (int xStep = 0; xStep < _xGranularity; xStep++) {
                double xRot = X_ROT_MIN + (((double) xStep / _xGranularity) * X_ROT_SPAN);
                var score = testDirection(level, vOrigin, xRot, yRot);
                scores.add(score);
            }
        }

        return computeFinalScore(scores, level, vOrigin, bOrigin);
    }

    protected abstract TScore testDirection(LevelReader<TBlockPos, TVec3, TBlockState> level, TVec3 origin, double xRot, double yRot);
    protected abstract double computeFinalScore(List<TScore> scores, LevelReader<TBlockPos, TVec3, TBlockState> level, TVec3 vOrigin, TBlockPos bOrigin);

    protected abstract TBlockPos offsetB(TBlockPos blockPos, int x, int y, int z);
    protected abstract TVec3 offsetV(TVec3 position, double x, double y, double z);
    protected abstract TBlockPos vectorToBlockPos(TVec3 position);
    protected abstract boolean isAir(TBlockState blockState);


    protected TBlockPos getNearestBlockOrAirAtBoundary(LevelReader<TBlockPos, TVec3, TBlockState> level, TVec3 fromVec, double xRot, double yRot) {
        double vecX = Math.sin(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));
        double vecY = Math.sin(-xRot * (Math.PI / 180.0));
        double vecZ = Math.cos(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));

        double xOff = vecX * _measureDistance;
        double yOff = vecY * _measureDistance;
        double zOff = vecZ * _measureDistance;

        TVec3 toVec = offsetV(fromVec, xOff, yOff, zOff);
        TBlockPos bPos = level.tryGetNearestBlockInDirection(fromVec, toVec);
        return bPos != null ? bPos : vectorToBlockPos(toVec);
    }

    protected double averageLightingOfAirAroundBlock(LevelReader<TBlockPos, TVec3, TBlockState> level, TBlockPos bPos) {
        List<TBlockPos> airPositions = getAirPositionsAroundBlock(level, bPos).toList();
        if (airPositions.isEmpty())
            return 0;

        int sum = airPositions.stream()
                .map(level::getMaxSkyLight)
                .reduce(0, Integer::sum);
        return (double) sum / airPositions.size();
    }

    protected Stream<TBlockPos> getAirPositionsAroundBlock(LevelReader<TBlockPos, TVec3, TBlockState> level, TBlockPos bPos) {
        return Stream.of( // Does not include diagonals
                offsetB(bPos, 1,0,0),
                offsetB(bPos, -1,0,0),
                offsetB(bPos, 0,1,0),
                offsetB(bPos, 0,-1,0),
                offsetB(bPos, 0,0,1),
                offsetB(bPos, 0,0,-1)
        ).filter(pos -> isAir(level.getBlockState(pos)));
    }
}
