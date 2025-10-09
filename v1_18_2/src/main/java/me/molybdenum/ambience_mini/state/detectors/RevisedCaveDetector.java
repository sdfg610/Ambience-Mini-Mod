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
    protected static final double Y_ROT_SKYWARD_THRESHOLD = 30.0;


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
        List<Measurement> scores = readings.stream().map(r -> measure(level, r, vOrigin)).toList();

        /*
        var finalScore = scores.stream().map(Score::sum).reduce(0.0, Double::sum) / scores.size();

        double caveBlockCount = scores.stream().filter(s -> s.materialScore() > 0.05 || s.tagScore() > 0.05).count();
        finalScore += 0.2 * (caveBlockCount / scores.size());

        double nonCaveBlockCount = scores.stream().filter(s -> s.materialScore() < -0.05 || s.tagScore() < -0.05).count();
        finalScore -= 0.2 * (nonCaveBlockCount / scores.size());

        //finalScore -= (0.2/MAX_LIGHT_LEVEL) * level.getMaxSkyLightAt(origin);

        // TODO: Use skyward property to determine sky-view?
        // TODO: Measure of enclosed-ness?
        // TODO: Less dependency on material and tags?
        // TODO: Use world-gen rates to determine whether something could be underground?


        //



        return finalScore;*/

        return 0;
    }


    protected Measurement measure(
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            BlockReading<BlockPos, BlockState> reading,
            Vec3 origin
    ) {
        BlockPos bPos = reading.blockPos();
        BlockState blockState = reading.blockState();

        boolean isSkyward = reading.yRot() >= Y_ROT_SKYWARD_THRESHOLD;
        int skyLight = level.getMaxSkyLightAt(bPos);
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

        return new Measurement(type, isSkyward, skyLight, blockLight, distance);
    }


    protected record Measurement(
            MaterialType type,
            boolean isSkyward,
            int skyLightLevel,
            int blockLightLevel,
            double distance
    ) { }

    protected enum MaterialType {
        AIR, CAVE, AMBIGUOUS, WEAK_NON_CAVE, NON_CAVE
    }
}
