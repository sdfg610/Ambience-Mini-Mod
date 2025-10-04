package me.molybdenum.ambience_mini.state;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CaveDetector {
    // RotX = sideways [-180.0 ; +179.9]
    // RotY = up/down [-90.0 ; +90.0]
    // (X:0,Y:0) = (south, horizontal)
    private static final int Y_ROT_GRANULARITY = 4; // The number of points along yRot to measure apart from up and down.
    private static final double Y_ROT_MIN = -90.0;
    private static final double Y_ROT_SPAN = 180.0;

    private static final int X_ROT_GRANULARITY = 8; // The total number of points around xRot to measure.
    private static final double X_ROT_MIN = -180.0;
    private static final double X_ROT_SPAN = 360.0;

    private static final int MEASURE_DISTANCE = 123; // The maximum distance from the player to consider blocks.

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
        add(BlockTags.MINEABLE_WITH_SHOVEL);
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
        add(BlockTags.MINEABLE_WITH_PICKAXE);
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
        add(Material.SAND);
        add(Material.SNOW);
        add(Material.WOOD);

    }};
    private static final ArrayList<Material> CAVE_MATERIALS = new ArrayList<>() {{
        add(Material.LAVA);
        add(Material.WEB);
        add(Material.SCULK);
        add(Material.STONE);
        add(Material.AMETHYST);
    }};


    public static double getAveragedCaveScore(ClientLevel level, Player player, int radius)
    {
        List<Double> scores = new ArrayList<>();
        for (int xOff = -radius; xOff <= radius; xOff++)
            for (int yOff = -radius; yOff <= radius; yOff++)
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    BlockPos bOrigin = player.eyeBlockPosition().offset(xOff, yOff, zOff);
                    Vec3 vOrigin = player.getEyePosition().add(xOff, yOff, zOff);
                    scores.add(getCaveScore(level, vOrigin, bOrigin));
                }

        return scores.stream().reduce(0.0, Double::sum) / scores.size();
    }

    public static double getCaveScore(ClientLevel level, Player player) {
        return getCaveScore(level, player.getEyePosition(), player.eyeBlockPosition());
    }

    // Returns a value in (approximately) the interval [-1.0 (not cave) ; 1.0 (yes cave)].
    private static double getCaveScore(ClientLevel level, Vec3 vOrigin, BlockPos bOrigin)
    {
        List<Double> scores = new ArrayList<>();
        testDirection(level, vOrigin, 0, 90.0).ifPresent(scores::add); // Straight up
        testDirection(level, vOrigin, 0, -90.0).ifPresent(scores::add); // Straight down

        for (int yStep = 0; yStep < Y_ROT_GRANULARITY; yStep++) {
            double yRot = Y_ROT_MIN + (((double)(yStep+1) / (Y_ROT_GRANULARITY+1)) * Y_ROT_SPAN);
            for (int xStep = 0; xStep < X_ROT_GRANULARITY; xStep++) {
                double xRot = X_ROT_MIN + (((double)xStep / X_ROT_GRANULARITY) * X_ROT_SPAN);
                testDirection(level, vOrigin, xRot, yRot).ifPresent(scores::add);
            }
        }
        var finalScore = scores.stream().reduce(0.0, Double::sum) / scores.size();

        if (level.canSeeSky(bOrigin))
            finalScore -= 0.2;
        finalScore -= getMaxSkyLight(level, bOrigin) * (0.33/15);

        return finalScore;
    }

    private static Optional<Double> testDirection(ClientLevel level, Vec3 origin, double xRot, double yRot)
    {
        BlockPos bPos = getBlockInDirection(level, origin, xRot, yRot);
        if (bPos == null)
            return Optional.empty();

        BlockState state = level.getBlockState(bPos);

        // Generate cave score based on tags [-0.33 ; +0.33]
        AtomicInteger caveTagsAt = new AtomicInteger();
        AtomicInteger noneCaveTagsAt = new AtomicInteger();
        state.getTags().forEach(tag -> {
            if (NON_CAVE_TAGS.contains(tag)) noneCaveTagsAt.getAndIncrement();
            else if (CAVE_TAGS.contains(tag)) caveTagsAt.getAndIncrement();
        });
        int caveTags = caveTagsAt.get(), noneCaveTags = noneCaveTagsAt.get();
        double tagScore = 0;
        if (noneCaveTags < caveTags) tagScore = (1.0/3.0) - ((1.0/3.0) * ((double)noneCaveTags / caveTags));
        else if (caveTags < noneCaveTags) tagScore = -(1.0/3.0) + ((1.0/3.0) * ((double)caveTags / noneCaveTags));

        // Generate cave score based on material [-0.33 ; +0.33]
        Material material = state.getMaterial();
        double materialScore = 0;
        if (CAVE_MATERIALS.contains(material)) materialScore = 1.0/3.0;
        if (NON_CAVE_MATERIALS.contains(material)) materialScore = -1.0/3.0;

        // Generate cave score based on maximal sky-lighting [-0.33 ; +0.33]
        double lightingScore = 0.5 - ((double)1/15) * getMaxSkyLight(level, bPos);

        // Use identifiers???? state.getBlock().getRegistryName().getPath()

        return Optional.of(tagScore + materialScore + lightingScore);
    }

    private static int getMaxSkyLight(ClientLevel level, BlockPos position) {
        return level.getBrightness(LightLayer.SKY, position);
    }

    private static @Nullable BlockPos getBlockInDirection(ClientLevel level, Vec3 origin, double xRot, double yRot) {
        double vecX = Math.sin(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));
        double vecY = Math.sin(-xRot * (Math.PI / 180.0));
        double vecZ = Math.cos(-yRot * (Math.PI / 180.0) - Math.PI) * -Math.cos(-xRot * (Math.PI / 180.0));

        var hit = level.clip(new ClipContext(
                origin,
                origin.add(vecX * MEASURE_DISTANCE, vecY * MEASURE_DISTANCE, vecZ * MEASURE_DISTANCE),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                null
        ));

        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }
}
