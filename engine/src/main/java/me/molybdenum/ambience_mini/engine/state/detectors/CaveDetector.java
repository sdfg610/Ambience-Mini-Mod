package me.molybdenum.ambience_mini.engine.state.detectors;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;
import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.BlockReading;
import me.molybdenum.ambience_mini.engine.state.readers.PlayerReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CaveDetector<TBlockPos, TVec3, TBlockState>
{
    protected static final double Y_ROT_SKYWARD_THRESHOLD = 45.0;

    double SKY_ACCESS_BASE_WEIGHT = 1;
    double SKY_LIGHT_BASE_WEIGHT = .5;
    double CAVE_BASE_WEIGHT = 1;

    private final int _caveScoreRadius;
    private final int _measureDistance;
    private final int _xGranularity;
    private final int _yGranularity;


    public CaveDetector(BaseConfig config) {
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
                        scores.add(computeScore(level, readings));
                    }
                }

        if (scores.isEmpty())
            return Optional.empty();

        return Optional.of(scores.stream().reduce(0.0, Double::sum) / scores.size());
    }

    protected double computeScore(
            BaseLevelReader<TBlockPos, TVec3, TBlockState> level,
            List<BlockReading<TBlockPos, TBlockState>> readings
    ){
        List<Measurement> measurements = readings.stream()
                .map(r -> measure(level, r))
                .toList();

        //
        // Light and material ratios
        //
        int measureCount = measurements.size();
        int caveCount = 0, weakCaveCount = 0, ambCount = 0, weakNonCaveCount = 0, nonCount = 0, airCount = 0;
        for (var m : measurements) {
            switch (m.type()) {
                case CAVE -> caveCount++;
                case WEAK_CAVE -> weakCaveCount++;
                case AMBIGUOUS -> ambCount++;
                case WEAK_NON_CAVE -> weakNonCaveCount++;
                case NON_CAVE -> nonCount++;
                case AIR -> airCount++;
            }
        }

        double averageAirLight = measurements.stream()
                .filter(m -> m.type() == MaterialType.AIR)
                .map(Measurement::maxSkyLightLevel)
                .reduce(0D, Double::sum)
                / measurements.size();
        double averageAirLightRatio = averageAirLight / BaseLevelReader.MAX_LIGHT_LEVEL;

        double strongTotal = caveCount + nonCount;
        double strongCaveRatio = strongTotal == 0 ? 0 : (double)caveCount / strongTotal;
        double strongNonCaveRatio = strongTotal == 0 ? 0 : (double)nonCount / strongTotal;

        double totalCaveRatio = (caveCount + .25*ambCount + (.25*weakCaveCount + .5*weakCaveCount*strongCaveRatio) + (airCount - airCount*averageAirLightRatio)) / measureCount;
        double totalNonCaveRatio = (nonCount + .25*ambCount + (.25*weakNonCaveCount+ .5*weakNonCaveCount*strongNonCaveRatio) + airCount*averageAirLightRatio) / measureCount;

        //
        // Weighted judgments
        //
        double antiSkyAccess = 0, proSkyAccess = 0;
        double antiSkyLight = 0, proSkyLight = 0;
        double antiCave = 0, proCave = 0;
        int maxArtificialLight = 0; double artificialLight = 0;
        for (var m : measurements) {
            double maxSkyLightRatio = m.maxSkyLightLevel() / BaseLevelReader.MAX_LIGHT_LEVEL;
            double inverseMaxSkyLightRatio = 1 - maxSkyLightRatio;
            if (m.isSkyward()) {
                switch (m.type()) {
                    case CAVE -> {
                        antiSkyAccess += .3 + .7 * inverseMaxSkyLightRatio;
                        proSkyAccess += .3 * maxSkyLightRatio;
                    }
                    case WEAK_CAVE -> {
                        antiSkyAccess += .15 + .5 * inverseMaxSkyLightRatio;
                        proSkyAccess += .2 * maxSkyLightRatio;
                    }
                    case AMBIGUOUS  -> {
                        antiSkyAccess += .3 * inverseMaxSkyLightRatio;
                        proSkyAccess += .3 * maxSkyLightRatio;
                    }
                    case WEAK_NON_CAVE -> {
                        antiSkyAccess += .2 * inverseMaxSkyLightRatio;
                        proSkyAccess += .15 + .5 * maxSkyLightRatio;
                    }
                    case NON_CAVE -> {
                        antiSkyAccess += .3 * inverseMaxSkyLightRatio;
                        proSkyAccess += .3 + .7 * maxSkyLightRatio;
                    }
                    case AIR -> {
                        antiSkyAccess += inverseMaxSkyLightRatio;
                        proSkyAccess += maxSkyLightRatio;
                    }
                }
            }

            double blockLightRatio = m.blockLightLevel() / BaseLevelReader.MAX_LIGHT_LEVEL;
            switch (m.type()) {
                case CAVE -> {
                    maxArtificialLight++;
                    artificialLight += .1 * blockLightRatio;

                    proCave += 1;

                    antiSkyLight += .5*inverseMaxSkyLightRatio;
                    proSkyLight += .2*maxSkyLightRatio;
                }
                case WEAK_CAVE -> {
                    maxArtificialLight++;
                    artificialLight += .2 * blockLightRatio;

                    antiCave += .25;
                    proCave += .75;

                    antiSkyLight += .75*inverseMaxSkyLightRatio;
                    proSkyLight += .25*maxSkyLightRatio;
                }
                case AMBIGUOUS -> {
                    maxArtificialLight++;
                    artificialLight += .3 * blockLightRatio;

                    antiCave += .5;
                    proCave += .5;

                    antiSkyLight += .5 * inverseMaxSkyLightRatio;
                    proSkyLight += .5 * maxSkyLightRatio;
                }
                case WEAK_NON_CAVE -> {
                    maxArtificialLight++;
                    artificialLight += .4 * blockLightRatio;

                    antiCave += .25;
                    proCave += .75;

                    antiSkyLight += .25*inverseMaxSkyLightRatio;
                    proSkyLight += .75*maxSkyLightRatio;
                }
                case NON_CAVE -> {
                    maxArtificialLight++;
                    artificialLight += .5 * blockLightRatio;

                    antiCave += 1;
                    proSkyLight += maxSkyLightRatio;
                }
                case AIR -> {
                    antiCave += maxSkyLightRatio;
                    proCave += inverseMaxSkyLightRatio;

                    antiSkyLight += inverseMaxSkyLightRatio;
                    proSkyLight += maxSkyLightRatio;
                }
            }
        }

        //
        // Scores. (Light-based scores are negated since "more light" gives lower cave score.)
        //
        double skyAccessWeight = SKY_ACCESS_BASE_WEIGHT + .75*totalNonCaveRatio;
        double skyAccessScore = -skyAccessWeight * computeAntiProScore(antiSkyAccess, proSkyAccess);

        double skyLightWeight = SKY_LIGHT_BASE_WEIGHT + .75*totalNonCaveRatio - .75*Math.max(0, skyAccessScore);
        double skyLightScore = -skyLightWeight * computeAntiProScore(antiSkyLight, proSkyLight);

        double caveWeight = CAVE_BASE_WEIGHT + totalCaveRatio;
        double caveScore = caveWeight * computeAntiProScore(antiCave, proCave);

        double totalScore = skyAccessScore + skyLightScore + caveScore;
        double totalWeight = skyAccessWeight + skyLightWeight + caveWeight;

        if (maxArtificialLight != 0) {
            double artificialLightScore = (artificialLight / maxArtificialLight) * (1+.5*totalNonCaveRatio-.5*totalCaveRatio);

            totalScore -= artificialLightScore;
            totalWeight += artificialLightScore;
        }

        return totalScore / totalWeight; // Average, weighted score
    }

    // Produces a score between [-1.0 ; +1.0]
    private double computeAntiProScore(double anti, double pro) {
        return anti <= pro
                ? 1 - anti / pro
                : -1 + pro / anti;
    }


    private Measurement measure(
            BaseLevelReader<TBlockPos, TVec3, TBlockState> level,
            BlockReading<TBlockPos, TBlockState> reading
    ) {
        TBlockPos bPos = reading.blockPos();
        TBlockState blockState = reading.blockState();

        boolean isSkyward = reading.yRot() >= Y_ROT_SKYWARD_THRESHOLD;
        double maxSkyLight = level.getAverageSkyLightingAround(bPos);
        double blockLight = level.getAverageBlockLightingAround(bPos);

        MaterialType type = MaterialType.AMBIGUOUS;
        if (level.isAir(blockState))
            type = MaterialType.AIR;
        else if (level.isCaveMaterial(blockState))
            type = MaterialType.CAVE;
        else if (level.isWeakCaveMaterial(blockState))
            type = MaterialType.WEAK_CAVE;
        else if (level.isWeakNonCaveMaterial(blockState))
            type = MaterialType.WEAK_NON_CAVE;
        else if (level.isNonCaveMaterial(blockState))
            type = MaterialType.NON_CAVE;

        return new Measurement(type, isSkyward, maxSkyLight, blockLight);
    }


    private record Measurement(
            MaterialType type,
            boolean isSkyward,
            double maxSkyLightLevel,
            double blockLightLevel
    ) { }

    private enum MaterialType {
        AIR, CAVE, WEAK_CAVE, AMBIGUOUS, WEAK_NON_CAVE, NON_CAVE
    }
}
