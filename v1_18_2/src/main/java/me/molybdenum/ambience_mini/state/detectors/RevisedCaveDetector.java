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

    double SKY_ACCESS_BASE_WEIGHT = .75;
    double SKY_LIGHT_BASE_WEIGHT = .75;
    double MIN_LIGHT_BASE_WEIGHT = 1;
    double CAVE_BASE_WEIGHT = 2;


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

        long measureCount = measurements.size();
        long caveCount = 0, ambCount = 0, weakCount = 0, nonCount = 0;

        int skyAccessMaxLevel = 0; double skyAccessLevel = 0;
        int skyLightMaxLevel = 0; double skyLightLevel = 0;
        int minimumLightMaxLevel = 0; double minimumLightLevel = 0; // Minimum at daytime, that is.
        int caveMaxLevel = 0; double caveLevel = 0;

        for (var m : measurements) {
            double maxSkyLightRatio = (double)m.maxSkyLightLevel() / BaseLevelReader.MAX_LIGHT_LEVEL;
            double minimumLightRatio = Math.max(maxSkyLightRatio, (double)m.blockLightLevel() / BaseLevelReader.MAX_LIGHT_LEVEL);

            if (m.isSkyward()) {
                skyAccessMaxLevel += 1;
                switch (m.type()) {
                    case CAVE -> skyAccessLevel += .5 * maxSkyLightRatio;
                    case AMBIGUOUS  -> skyAccessLevel += .7 * maxSkyLightRatio;
                    case WEAK_NON_CAVE -> skyAccessLevel += .9 * maxSkyLightRatio;
                    case NON_CAVE, AIR -> skyAccessLevel += maxSkyLightRatio;
                }
            }

            skyLightMaxLevel += 1;
            switch (m.type()) {
                case CAVE -> {
                    caveCount++;
                    skyLightLevel += .5 * maxSkyLightRatio;
                    caveMaxLevel += 1; caveLevel += 1;
                }
                case AMBIGUOUS -> {
                    ambCount++;
                    skyLightLevel += .75 * maxSkyLightRatio;
                    minimumLightMaxLevel += 1; minimumLightLevel += .75 * minimumLightRatio;
                }
                case WEAK_NON_CAVE -> {
                    weakCount++;
                    skyLightLevel += .85 * maxSkyLightRatio;
                    caveMaxLevel += 1; caveLevel += .5;
                    minimumLightMaxLevel += 1; minimumLightLevel += .85 * minimumLightRatio;
                }
                case NON_CAVE -> {
                    nonCount++;
                    skyLightLevel += maxSkyLightRatio;
                    caveMaxLevel += 1;
                    minimumLightMaxLevel += 1; minimumLightLevel += minimumLightRatio;
                }
                case AIR -> skyLightLevel += maxSkyLightRatio;
            }
        }

        double inverseCaveRatio = 1.0 - ((double)caveCount / measureCount);

        // Negated since "more light" gives lower cave score.
        double skyAccessWeight = SKY_ACCESS_BASE_WEIGHT + .5*inverseCaveRatio;
        double skyAccessScore = -computeScore(skyAccessWeight, skyAccessLevel / skyAccessMaxLevel);

        // Negated since "more light" gives lower cave score.
        double skyLightWeight = SKY_LIGHT_BASE_WEIGHT + .5*inverseCaveRatio;
        double skyLightScore = -computeScore(skyLightWeight, skyLightLevel / skyLightMaxLevel);

        double totalScore = skyAccessScore + skyLightScore;
        double totalWeight = skyAccessWeight + skyLightWeight;

        if (minimumLightMaxLevel != 0) {
            double minimumLightScoreWeight = MIN_LIGHT_BASE_WEIGHT * (double)(ambCount + weakCount + nonCount) / measureCount;

            totalScore -= computeScore(minimumLightScoreWeight, minimumLightLevel / minimumLightMaxLevel);
            totalWeight += minimumLightScoreWeight;
        }

        if (caveMaxLevel != 0) {
            double caveScoreWeight = CAVE_BASE_WEIGHT * (double)(caveCount + nonCount + weakCount) / measureCount;

            totalScore += computeScore(caveScoreWeight, caveLevel / caveMaxLevel);
            totalWeight += caveScoreWeight;
        }

        return totalScore / totalWeight; // Average, weighted score
    }

    private double computeScore(double weight, double ratio) {
        return weight*(-1 + 2*ratio);
    }


    private Measurement measure(
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            BlockReading<BlockPos, BlockState> reading,
            Vec3 ignored
    ) {
        BlockPos bPos = reading.blockPos();
        BlockState blockState = reading.blockState();

        boolean isSkyward = reading.yRot() >= Y_ROT_SKYWARD_THRESHOLD;
        int maxSkyLight = level.getMaxSkyLightAt(bPos);
        int blockLight = level.getBlockLightAt(bPos);

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
            int maxSkyLightLevel,
            int blockLightLevel
    ) { }

    private enum MaterialType {
        AIR, CAVE, AMBIGUOUS, WEAK_NON_CAVE, NON_CAVE
    }
}
