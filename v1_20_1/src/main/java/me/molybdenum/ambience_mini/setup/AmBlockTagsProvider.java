package me.molybdenum.ambience_mini.setup;

import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AmBlockTagsProvider extends BlockTagsProvider
{
    public AmBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Common.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider ignored)
    {
        // Cave material specifically does not include "Tags.Blocks.STONE" since this tag contains too many/diverse blocks
        tag(AmTags.CAVE_MATERIAL)
                .addTag(BlockTags.BASE_STONE_OVERWORLD) // Stone, Andesite, Granite, Diorite, Deepslate, Tuff
                .addTag(BlockTags.CAVE_VINES)
                .addTag(BlockTags.CRYSTAL_SOUND_BLOCKS)
                .addTag(Tags.Blocks.ORES) // All the default ores.
                .addTag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                .addTag(Tags.Blocks.ORES_IN_GROUND_STONE)
                .add(Blocks.BEDROCK)
                .add(Blocks.COBWEB)
                .add(Blocks.GLOW_LICHEN)
                .add(Blocks.CALCITE)
                .add(Blocks.POINTED_DRIPSTONE)
                .add(Blocks.DRIPSTONE_BLOCK)
        ;

        tag(AmTags.WEAK_CAVE_MATERIAL)
                .addTag(Tags.Blocks.COBBLESTONE) // Different cobblestones and likely more by other mods
                .addTag(Tags.Blocks.GRAVEL) // Gravel and likely more by other mods
                .addTag(Tags.Blocks.OBSIDIAN) // Obsidian and likely more by other mods
                .addTag(BlockTags.TERRACOTTA)
        ;

        tag(AmTags.WEAK_NON_CAVE_MATERIAL)
                .addTag(BlockTags.PRESSURE_PLATES)
                .addTag(BlockTags.BUTTONS)
                .addTag(BlockTags.WOODEN_DOORS)
                .addTag(BlockTags.WOODEN_TRAPDOORS)
                .addTag(BlockTags.FENCES)
                .addTag(BlockTags.STAIRS)
                .addTag(BlockTags.FENCE_GATES)
                .addTag(BlockTags.SHULKER_BOXES)
                .addTag(BlockTags.CAULDRONS)
                .addTag(BlockTags.ANVIL)
                .addTag(Tags.Blocks.CHESTS)
                .add(Blocks.COMPOSTER)
                .add(Blocks.CRAFTING_TABLE)
                .add(Blocks.FURNACE)
                .add(Blocks.BLAST_FURNACE)
                .add(Blocks.ENCHANTING_TABLE)
                .add(Blocks.BREWING_STAND)
                .add(Blocks.CAULDRON)
                .add(Blocks.LOOM)
                .add(Blocks.BARREL)
                .add(Blocks.SMOKER)
                .add(Blocks.GRINDSTONE)
                .add(Blocks.LECTERN)
                .add(Blocks.SMITHING_TABLE)
        ;

        registerConcrete(tag(AmTags.NON_CAVE_MATERIAL))
                .addTag(BlockTags.SNOW)
                .addTag(BlockTags.ICE)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.CORAL_PLANTS)
                .addTag(BlockTags.WALL_CORALS)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.OVERWORLD_NATURAL_LOGS)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.BEEHIVES)
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.SAPLINGS)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.SAND)
                .addTag(BlockTags.IMPERMEABLE)
                .addTag(BlockTags.CAMPFIRES)
                .addTag(BlockTags.CANDLES)
                .addTag(BlockTags.BEDS)
                .addTag(Tags.Blocks.SAND)
                .addTag(Tags.Blocks.SANDSTONE)
                .addTag(Tags.Blocks.GLASS)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.GRASS)
                .add(Blocks.COARSE_DIRT)
                .add(Blocks.MUD)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.SEAGRASS)
                .add(Blocks.TALL_SEAGRASS)
                .add(Blocks.BOOKSHELF)
                .add(Blocks.FARMLAND)
                .add(Blocks.SUGAR_CANE)
                .add(Blocks.REDSTONE_LAMP)
                .add(Blocks.HAY_BLOCK)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.KELP)
                .add(Blocks.KELP_PLANT)
                .add(Blocks.TURTLE_EGG)
                .add(Blocks.FERN)
                .add(Blocks.CACTUS)
        ;
    }

    private IntrinsicTagAppender<Block> registerConcrete(IntrinsicTagAppender<Block> ta)
    {
        return ta
                .add(Blocks.WHITE_CONCRETE)
                .add(Blocks.ORANGE_CONCRETE)
                .add(Blocks.MAGENTA_CONCRETE)
                .add(Blocks.LIGHT_BLUE_CONCRETE)
                .add(Blocks.YELLOW_CONCRETE)
                .add(Blocks.LIME_CONCRETE)
                .add(Blocks.PINK_CONCRETE)
                .add(Blocks.GRAY_CONCRETE)
                .add(Blocks.LIGHT_GRAY_CONCRETE)
                .add(Blocks.CYAN_CONCRETE)
                .add(Blocks.PURPLE_CONCRETE)
                .add(Blocks.BLUE_CONCRETE)
                .add(Blocks.BROWN_CONCRETE)
                .add(Blocks.GREEN_CONCRETE)
                .add(Blocks.RED_CONCRETE)
                .add(Blocks.BLACK_CONCRETE)
                .add(Blocks.WHITE_CONCRETE_POWDER)
                .add(Blocks.ORANGE_CONCRETE_POWDER)
                .add(Blocks.MAGENTA_CONCRETE_POWDER)
                .add(Blocks.LIGHT_BLUE_CONCRETE_POWDER)
                .add(Blocks.YELLOW_CONCRETE_POWDER)
                .add(Blocks.LIME_CONCRETE_POWDER)
                .add(Blocks.PINK_CONCRETE_POWDER)
                .add(Blocks.GRAY_CONCRETE_POWDER)
                .add(Blocks.LIGHT_GRAY_CONCRETE_POWDER)
                .add(Blocks.CYAN_CONCRETE_POWDER)
                .add(Blocks.PURPLE_CONCRETE_POWDER)
                .add(Blocks.BLUE_CONCRETE_POWDER)
                .add(Blocks.BROWN_CONCRETE_POWDER)
                .add(Blocks.GREEN_CONCRETE_POWDER)
                .add(Blocks.RED_CONCRETE_POWDER)
                .add(Blocks.BLACK_CONCRETE_POWDER)
        ;
    }

    @Override
    public @NotNull String getName() {
        return "Ambience Mini Block-Tags Provider";
    }
}
