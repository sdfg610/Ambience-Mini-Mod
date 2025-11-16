package me.molybdenum.ambience_mini.setup;

import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AmBlockTagsProvider extends BlockTagsProvider
{
    public AmBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Common.MOD_ID, existingFileHelper);
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
                .addTag(Tags.Blocks.CLUSTERS)
                .add(Blocks.BEDROCK)
                .add(Blocks.COBWEB)
                .add(Blocks.GLOW_LICHEN)
                .add(Blocks.CALCITE)
                .add(Blocks.POINTED_DRIPSTONE)
                .add(Blocks.DRIPSTONE_BLOCK)
        ;

        tag(AmTags.WEAK_CAVE_MATERIAL)
                .addTag(Tags.Blocks.COBBLESTONES) // Different cobblestones and likely more by other mods
                .addTag(Tags.Blocks.GRAVELS) // Gravel and likely more by other mods
                .addTag(Tags.Blocks.OBSIDIANS) // Obsidian and likely more by other mods
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
                .add(Blocks.POWDER_SNOW_CAULDRON)
                .add(Blocks.LOOM)
                .add(Blocks.BARREL)
                .add(Blocks.SMOKER)
                .add(Blocks.GRINDSTONE)
                .add(Blocks.LECTERN)
                .add(Blocks.SMITHING_TABLE)
        ;

        tag(AmTags.NON_CAVE_MATERIAL)
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
                .addTag(BlockTags.CONCRETE_POWDER)
                .addTag(Tags.Blocks.CONCRETES)
                .addTag(Tags.Blocks.SANDS)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(Tags.Blocks.VILLAGER_JOB_SITES)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.SHORT_GRASS)
                .add(Blocks.COARSE_DIRT)
                .add(Blocks.MUD)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.OAK_SAPLING)
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

    @Override
    public @NotNull String getName() {
        return "Ambience Mini Block-Tags Provider";
    }
}
