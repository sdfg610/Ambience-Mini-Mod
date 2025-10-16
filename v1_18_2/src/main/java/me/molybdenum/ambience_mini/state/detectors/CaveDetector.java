package me.molybdenum.ambience_mini.state.detectors;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;
import me.molybdenum.ambience_mini.engine.state.detectors.BaseCaveDetector;
import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.BlockReading;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import java.util.*;

import static me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader.MAX_LIGHT_LEVEL;

public class CaveDetector extends BaseCaveDetector<BlockPos, Vec3, BlockState>
{
    protected static final double Y_ROT_SKYWARD_THRESHOLD = 30.0;
    protected static final double SCORE_WEIGHT = 1.0 / 3.0;

    private static final ArrayList<TagKey<Block>> NON_CAVE_TAGS = new ArrayList<>() {{
        add(BlockTags.SAND);
        add(BlockTags.DIRT);
        add(BlockTags.LOGS);
        add(BlockTags.CORAL_BLOCKS);
        add(BlockTags.WALL_CORALS);
        add(BlockTags.CORAL_PLANTS);
        add(BlockTags.CORALS);
        add(BlockTags.PLANKS);
        add(BlockTags.STONE_BRICKS);
        add(BlockTags.BEEHIVES);
        add(BlockTags.CROPS);
        add(BlockTags.SNOW);
        add(BlockTags.ICE);
        add(Tags.Blocks.SAND);
        add(Tags.Blocks.SANDSTONE);
    }};
    private static final ArrayList<TagKey<Block>> CAVE_TAGS = new ArrayList<>() {{
        add(BlockTags.GOLD_ORES);
        add(BlockTags.IRON_ORES);
        add(BlockTags.DIAMOND_ORES);
        add(BlockTags.REDSTONE_ORES);
        add(BlockTags.LAPIS_ORES);
        add(BlockTags.EMERALD_ORES);
        add(BlockTags.COPPER_ORES);
        add(BlockTags.BASE_STONE_OVERWORLD);
        add(BlockTags.STONE_ORE_REPLACEABLES);
        add(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        add(BlockTags.CRYSTAL_SOUND_BLOCKS);
        add(BlockTags.CAVE_VINES);
        add(Tags.Blocks.COBBLESTONE);
        add(Tags.Blocks.GRAVEL);
        add(Tags.Blocks.OBSIDIAN);
        add(Tags.Blocks.ORES);
        add(Tags.Blocks.STONE);
    }};

    private static final ArrayList<Material> NON_CAVE_MATERIALS = new ArrayList<>() {{
        add(Material.DECORATION);
        add(Material.GRASS);
        add(Material.ICE_SOLID);
        add(Material.ICE);
        add(Material.SNOW);
        add(Material.POWDER_SNOW);
        add(Material.WOOD);

    }};
    private static final ArrayList<Material> CAVE_MATERIALS = new ArrayList<>() {{
        add(Material.LAVA);
        add(Material.WEB);
        add(Material.SCULK);
        add(Material.STONE);
        add(Material.AMETHYST);
    }};


    public CaveDetector(BaseConfig config) {
        super(config);
    }

    @Override
    protected double computeScore(
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            List<BlockReading<BlockPos, BlockState>> readings,
            Vec3 vOrigin,
            BlockPos bOrigin
    ) {
        List<Score> scores = readings.stream().map(r -> getScore(level, r)).toList();

        var finalScore = scores.stream().map(Score::sum).reduce(0.0, Double::sum) / scores.size();

        double caveBlockCount = scores.stream().filter(s -> s.materialScore() > 0.05 || s.tagScore() > 0.05).count();
        finalScore += 0.2 * (caveBlockCount / scores.size());

        double nonCaveBlockCount = scores.stream().filter(s -> s.materialScore() < -0.05 || s.tagScore() < -0.05).count();
        finalScore -= 0.2 * (nonCaveBlockCount / scores.size());

        finalScore -= (0.1/MAX_LIGHT_LEVEL) * level.getMaxSkyLightAt(bOrigin);

        return finalScore;
    }


    protected Score getScore(
            BaseLevelReader<BlockPos, Vec3, BlockState> level,
            BlockReading<BlockPos, BlockState> reading
    ) {
        boolean isSkyward = reading.yRot() >= Y_ROT_SKYWARD_THRESHOLD;

        BlockPos bPos = reading.blockPos();
        BlockState blockState = reading.blockState();
        if (level.isAir(blockState)) {
            double lightingScore = 1.0 - (2.0/MAX_LIGHT_LEVEL) * level.getMaxSkyLightAt(bPos);
            return new Score(0, 0, lightingScore, isSkyward);
        }

        // Generate cave score based on tags [-0.33 ; +0.33]
        int caveTags = (int)blockState.getTags().filter(CAVE_TAGS::contains).count();
        int noneCaveTags = (int)blockState.getTags().filter(NON_CAVE_TAGS::contains).count();
        double tagScore = 0;
        if (noneCaveTags < caveTags) tagScore = SCORE_WEIGHT - (SCORE_WEIGHT*2 * ((double) noneCaveTags / caveTags));
        else if (caveTags < noneCaveTags) tagScore = -SCORE_WEIGHT + (SCORE_WEIGHT*2 * ((double) caveTags / noneCaveTags));

        // Generate cave score based on material [-0.33 ; +0.33]
        double materialScore = 0;
        if (CAVE_MATERIALS.contains(blockState.getMaterial())) materialScore = SCORE_WEIGHT;
        if (NON_CAVE_MATERIALS.contains(blockState.getMaterial())) materialScore = -SCORE_WEIGHT;

        // Generate cave score based on maximal sky-lighting [-0.33 ; +0.33]
        double lightingScore = SCORE_WEIGHT - (SCORE_WEIGHT*2 / MAX_LIGHT_LEVEL) * level.getAverageSkyLightingAround(bPos);
        //double lightingScore = 0.33 * (1.5 - (log2(30 - averageLight*2 + 2) / 2)); // Quick dropoff such that only sharp light counts as non-cave

        return new Score(tagScore, materialScore, lightingScore, isSkyward);
    }
}
