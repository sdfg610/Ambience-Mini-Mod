package me.molybdenum.ambience_mini.state.detectors;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;
import me.molybdenum.ambience_mini.engine.state.detectors.BaseCaveDetector;
import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.BlockReading;
import me.molybdenum.ambience_mini.setup.AmTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RevisedCaveDetector extends BaseCaveDetector<BlockPos, Vec3, BlockState>
{
    protected static final double Y_ROT_SKYWARD_THRESHOLD = 45.0;

    double SKY_ACCESS_BASE_WEIGHT = 1;
    double SKY_LIGHT_BASE_WEIGHT = .5;
    double ARTIFICIAL_LIGHT_BASE_WEIGHT = .5;
    double CAVE_BASE_WEIGHT = 1;


    public RevisedCaveDetector(BaseConfig config) {
        super(config);
    }

    @Override
    protected double computeScore(
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            List<BlockReading<BlockPos, BlockState>> readings,
            Vec3 vOrigin,
            BlockPos bOrigin
    ) {
        List<Measurement> measurements = readings.stream()
                .map(r -> measure(level, r, vOrigin))
                .toList();

        //
        // Light and material ratios
        //
        int measureCount = measurements.size();
        int caveCount = 0, ambCount = 0, weakCount = 0, nonCount = 0, airCount = 0;
        for (var m : measurements) {
            switch (m.type()) {
                case CAVE -> caveCount++;
                case AMBIGUOUS -> ambCount++;
                case WEAK_NON_CAVE -> weakCount++;
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

        double caveRatio = (caveCount + .25*ambCount + (airCount - airCount*averageAirLightRatio)) / measureCount;
        double nonCaveRatio = (nonCount + .25*ambCount + .5*weakCount + airCount*averageAirLightRatio) / measureCount;

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
                    artificialLight += .05 * blockLightRatio;

                    proCave += 1;

                    antiSkyLight += .5*inverseMaxSkyLightRatio;
                    proSkyLight += .2*maxSkyLightRatio;
                }
                case AMBIGUOUS -> {
                    maxArtificialLight++;
                    artificialLight += .1 * blockLightRatio;

                    antiCave += .5;
                    proCave += .5;

                    antiSkyLight += .5 * inverseMaxSkyLightRatio;
                    proSkyLight += .5 * maxSkyLightRatio;
                }
                case WEAK_NON_CAVE -> {
                    maxArtificialLight++;
                    artificialLight += .3 * blockLightRatio;

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
        double skyAccessWeight = SKY_ACCESS_BASE_WEIGHT + .75*nonCaveRatio;
        double skyAccessScore = -skyAccessWeight * computeAntiProScore(antiSkyAccess, proSkyAccess);

        double skyLightWeight = SKY_LIGHT_BASE_WEIGHT + .75*nonCaveRatio - .75*Math.max(0, skyAccessScore);
        double skyLightScore = -skyLightWeight * computeAntiProScore(antiSkyLight, proSkyLight);

        double caveWeight = CAVE_BASE_WEIGHT + caveRatio;
        double caveScore = caveWeight * computeAntiProScore(antiCave, proCave);

        double totalScore = skyAccessScore + skyLightScore + caveScore;
        double totalWeight = skyAccessWeight + skyLightWeight + caveWeight;

        if (maxArtificialLight != 0) {
            double artificialLightScore = (artificialLight / maxArtificialLight) * (1+.5*nonCaveRatio);

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
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            BlockReading<BlockPos, BlockState> reading,
            Vec3 ignored
    ) {
        BlockPos bPos = reading.blockPos();
        BlockState blockState = reading.blockState();

        boolean isSkyward = reading.yRot() >= Y_ROT_SKYWARD_THRESHOLD;
        double maxSkyLight = level.getAverageSkyLightingAround(bPos);
        double blockLight = level.getAverageBlockLightingAround(bPos);

        MaterialType type = MaterialType.AMBIGUOUS;
        if (level.isAir(blockState))
            type = MaterialType.AIR;
        else if (blockState.is(AmTags.CAVE_MATERIAL))
            type = MaterialType.CAVE;
        else if (blockState.is(AmTags.WEAK_NON_CAVE_MATERIAL))
            type = MaterialType.WEAK_NON_CAVE;
        else if (blockState.is(AmTags.NON_CAVE_MATERIAL))
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
        AIR, CAVE, AMBIGUOUS, WEAK_NON_CAVE, NON_CAVE
    }
}
