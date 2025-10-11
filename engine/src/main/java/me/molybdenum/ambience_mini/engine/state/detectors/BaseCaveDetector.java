package me.molybdenum.ambience_mini.engine.state.detectors;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;
import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.BlockReading;
import me.molybdenum.ambience_mini.engine.state.readers.PlayerReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseCaveDetector<TBlockPos, TVec3, TBlockState>
{
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


    public Optional<Double> getAveragedCaveScore(
            BaseLevelReader<TBlockPos, TVec3, TBlockState> level, PlayerReader<TBlockPos, TVec3> player
    ) {
        List<Double> scores = new ArrayList<>();
        for (int xOff = -_caveScoreRadius; xOff <= _caveScoreRadius; xOff++)
            for (int yOff = -_caveScoreRadius; yOff <= _caveScoreRadius; yOff++)
                for (int zOff = -_caveScoreRadius; zOff <= _caveScoreRadius; zOff++) {
                    TBlockPos bOrigin = level.offsetBlockPos(player.eyeBlockPos(), xOff, yOff, zOff);
                    if (level.isAirAt(bOrigin)) {
                        TVec3 vOrigin = level.offsetVector(player.eyePosition(), xOff, yOff, zOff);
                        List<BlockReading<TBlockPos, TBlockState>> readings
                                = level.readSurroundings(vOrigin, _xGranularity, _yGranularity, _measureDistance);
                        scores.add(computeScore(level, readings, vOrigin, bOrigin));
                    }
                }

        if (scores.isEmpty())
            return Optional.empty();

        return Optional.of(scores.stream().reduce(0.0, Double::sum) / scores.size());
    }

    protected abstract double computeScore(
            BaseLevelReader<TBlockPos, TVec3, TBlockState> level,
            List<BlockReading<TBlockPos, TBlockState>> readings,
            TVec3 vOrigin,
            TBlockPos bOrigin
    );
}
