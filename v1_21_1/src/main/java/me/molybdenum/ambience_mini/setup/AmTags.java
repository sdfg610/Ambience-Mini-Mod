package me.molybdenum.ambience_mini.setup;

import me.molybdenum.ambience_mini.AmbienceMini;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class AmTags {
    // Always counts towards being in a cave
    public static final TagKey<Block> CAVE_MATERIAL = create("cave_material");

    // These will count more towards being cave if other cave materials are present
    public static final TagKey<Block> WEAK_CAVE_MATERIAL = create("weak_cave_material");

    // These will count more towards being non-cave if other non-cave materials are present
    public static final TagKey<Block> WEAK_NON_CAVE_MATERIAL = create("weak_non_cave_material");

    // Always counts towards NOT being in a cave
    public static final TagKey<Block> NON_CAVE_MATERIAL = create("non_cave_material");

    // Blocks not in any above category are ambiguous and will use lighting and the surroundings to determine a score.

    private static TagKey<Block> create(String name)
    {
        return BlockTags.create(AmbienceMini.rl(name));
    }
}
