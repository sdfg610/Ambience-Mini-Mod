package me.molybdenum.ambience_mini.state.detectors;

import me.molybdenum.ambience_mini.engine.BaseConfig;
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
        List<Measurement> measurements = readings
                .stream()
                .map(r -> measure(level, r, vOrigin))
                .toList();

        long measureCount = measurements.size();
        long skywardCount = 0, skyAccessCount = 0;

        double minDist = Double.MAX_VALUE, maxDist = Double.MIN_VALUE;
        // TODO: Pure min-dist/max-dist is dangerous if one direction is unobstructed. Perhaps base on some percentile?

        for (var m : measurements) {
            minDist = Double.min(minDist, m.distance());
            maxDist = Double.max(maxDist, m.distance());

            if (m.isSkyward()) skywardCount++;
            if (m.type() == MaterialType.AIR) {
                if (m.isSkyward() && m.maxSkyLightLevel() >= 14)
                    skyAccessCount++;
            }
        }

        double caveScore = 0;
        for (var m : measurements) {
            switch (m.type()) {
                case AIR -> { }
                case CAVE -> { }
                case AMBIGUOUS -> { }
                case WEAK_NON_CAVE -> { }
                case NON_CAVE -> { }
            }
        }






/*


        for (var m : measurements) {
            switch (m.type()) {
                case AIR -> airCount++;
                case CAVE -> caveCount++;
                case AMBIGUOUS -> ambiguousCount++;
                case WEAK_NON_CAVE -> weakCount++;
                case NON_CAVE -> nonCaveCount++;
            }
        }

        // Only pulls towards "not in cave"
        double accessToSkyRatio = (double)skyAccessCount / skywardCount;
        double nonCaveRatio = (double)nonCaveCount / measureCount;
        double weakNonCaveRatio = (double)weakCount / measureCount;

        // Only pulls towards "in cave"
        double caveRatio = (double)caveCount / measureCount;

        // Ambiguous measurements
        double ambiguousRatio = (double)ambiguousCount / measureCount;

*/



        //double baseMaterialRatioScore = 1 - (2*Math.max())

        /*
        var finalScore = scores.stream().map(Score::sum).reduce(0.0, Double::sum) / scores.size();

        double caveBlockCount = scores.stream().filter(s -> s.materialScore() > 0.05 || s.tagScore() > 0.05).count();
        finalScore += 0.2 * (caveBlockCount / scores.size());

        double nonCaveBlockCount = scores.stream().filter(s -> s.materialScore() < -0.05 || s.tagScore() < -0.05).count();
        finalScore -= 0.2 * (nonCaveBlockCount / scores.size());

        //finalScore -= (0.2/MAX_LIGHT_LEVEL) * level.getMaxSkyLightAt(origin);

        return finalScore;
        */

        return 0;
    }


    private Measurement measure(
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            BlockReading<BlockPos, BlockState> reading,
            Vec3 origin
    ) {
        BlockPos bPos = reading.blockPos();
        BlockState blockState = reading.blockState();

        boolean isSkyward = reading.yRot() >= Y_ROT_SKYWARD_THRESHOLD;
        int maxSkyLight = level.getMaxSkyLightAt(bPos);
        int blockLight = level.getBlockLightAt(bPos);
        double distance = origin.distanceTo(origin);

        MaterialType type = MaterialType.AMBIGUOUS;
        if (level.isAir(blockState))
            type = MaterialType.AIR;
        else if (blockState.is(AmTags.CAVE_MATERIAL))
            type = MaterialType.CAVE;
        else if (blockState.is(AmTags.WEAK_NON_CAVE_MATERIAL))
            type = MaterialType.WEAK_NON_CAVE;
        else if (blockState.is(AmTags.NON_CAVE_MATERIAL))
            type = MaterialType.NON_CAVE;

        return new Measurement(type, isSkyward, maxSkyLight, blockLight, distance);
    }


    private record Measurement(
            MaterialType type,
            boolean isSkyward,
            int maxSkyLightLevel,
            int blockLightLevel,
            double distance
    ) { }

    private enum MaterialType {
        AIR, CAVE, AMBIGUOUS, WEAK_NON_CAVE, NON_CAVE
    }
}
