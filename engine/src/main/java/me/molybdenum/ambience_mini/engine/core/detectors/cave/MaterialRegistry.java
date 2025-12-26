package me.molybdenum.ambience_mini.engine.core.detectors.cave;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

public class MaterialRegistry {
    private static final HashMap<String, MaterialType> TAG_MATERIALS = new HashMap<>();
    private static final HashMap<String, MaterialType> BLOCK_MATERIALS = new HashMap<>();


    static {
        initializeTagMaterials();
        initializeBlockMaterials();
    }

    private static void initializeTagMaterials() {
        // Cave
        registerTags(MaterialType.CAVE)
                .register("minecraft:base_stone_overworld")
                .register("minecraft:cave_vines")
                .register("minecraft:crystal_sound_blocks")
                .register("forge:ores")
                .register("forge:ores_in_ground/deepslate")
                .register("forge:ores_in_ground/stone")
                .register("c:ores")
                .register("c:ores_in_ground/deepslate")
                .register("c:ores_in_ground/stone")
                .register("c:clusters")
        ;

        // Weak Cave
        registerTags(MaterialType.WEAK_CAVE)
                .register("forge:cobblestone")
                .register("forge:gravel")
                .register("forge:obsidian")
                .register("c:cobblestones")
                .register("c:gravels")
                .register("c:obsidians")
                .register("minecraft:terracotta")
        ;

        // Weak Non-Cave
        registerTags(MaterialType.WEAK_NON_CAVE)
                .register("minecraft:pressure_plates")
                .register("minecraft:buttons")
                .register("minecraft:wooden_doors")
                .register("minecraft:wooden_trapdoors")
                .register("minecraft:fences")
                .register("minecraft:fence_gates")
                .register("minecraft:stairs")
                .register("minecraft:shulker_boxes")
                .register("minecraft:cauldrons")
                .register("minecraft:anvil")
                .register("forge:chests")
                .register("c:chests")
        ;

        // Non-Cave
        registerTags(MaterialType.NON_CAVE)
                .register("minecraft:snow")
                .register("minecraft:ice")
                .register("minecraft:coral_blocks")
                .register("minecraft:coral_plants")
                .register("minecraft:wall_corals")
                //.register("minecraft:logs")
                .register("minecraft:overworld_natural_logs") // Valid from 1.19 and up
                .register("minecraft:leaves")
                .register("minecraft:beehives")
                .register("minecraft:crops")
                .register("minecraft:saplings")
                .register("minecraft:carpets")
                .register("minecraft:wool_carpets")
                .register("minecraft:flowers")
                .register("minecraft:sand")
                .register("minecraft:impermeable")
                .register("minecraft:campfires")
                .register("minecraft:candles")
                .register("minecraft:beds")
                .register("minecraft:concrete_powder")
                .register("forge:sand")
                .register("forge:sandstone")
                .register("forge:glass")
                .register("c:concretes")
                .register("c:sands")
                .register("c:sandstone/blocks")
                .register("c:glass_blocks")
                .register("c:glass_panes")
                .register("c:villager_job_sites")
        ;
    }

    private static void initializeBlockMaterials() {
        // Cave
        registerBlocks(MaterialType.CAVE)
                .register("minecraft:bedrock")
                .register("minecraft:cobweb")
                .register("minecraft:glow_lichen")
                .register("minecraft:calcite")
                .register("minecraft:pointed_dripstone")
                .register("minecraft:dripstone_block")
        ;

        // Weak Cave (none)

        // Weak Non-Cave
        registerBlocks(MaterialType.WEAK_NON_CAVE)
                .register("minecraft:composter")
                .register("minecraft:crafting_table")
                .register("minecraft:furnace")
                .register("minecraft:blast_furnace")
                .register("minecraft:enchanting_table")
                .register("minecraft:brewing_stand")
                .register("minecraft:loom")
                .register("minecraft:barrel")
                .register("minecraft:smoker")
                .register("minecraft:grindstone")
                .register("minecraft:lectern")
                .register("minecraft:smithing_table")
                .register("minecraft:grass_block")
                .register("minecraft:grass")
                .register("minecraft:tall_grass")
                .register("minecraft:short_grass")
        ;

        // Non-Cave
        registerBlocks(MaterialType.NON_CAVE)
                .register("minecraft:coarse_dirt")
                .register("minecraft:mud")
                .register("minecraft:seagrass")
                .register("minecraft:tall_seagrass")
                .register("minecraft:bookshelf")
                .register("minecraft:farmland")
                .register("minecraft:sugar_cane")
                .register("minecraft:redstone_lamp")
                .register("minecraft:hay_block")
                .register("minecraft:kelp")
                .register("minecraft:kelp_plant")
                .register("minecraft:turtle_egg")
                .register("minecraft:fern")
                .register("minecraft:cactus")
        ;
        registerConcreteBlocks();
    }

    private static void registerConcreteBlocks() {
        registerBlocks(MaterialType.NON_CAVE)
                .register("minecraft:white_concrete")
                .register("minecraft:orange_concrete")
                .register("minecraft:magenta_concrete")
                .register("minecraft:light_blue_concrete")
                .register("minecraft:yellow_concrete")
                .register("minecraft:lime_concrete")
                .register("minecraft:pink_concrete")
                .register("minecraft:gray_concrete")
                .register("minecraft:light_gray_concrete")
                .register("minecraft:cyan_concrete")
                .register("minecraft:purple_concrete")
                .register("minecraft:blue_concrete")
                .register("minecraft:brown_concrete")
                .register("minecraft:green_concrete")
                .register("minecraft:red_concrete")
                .register("minecraft:black_concrete")

                .register("minecraft:white_concrete_powder")
                .register("minecraft:orange_concrete_powder")
                .register("minecraft:magenta_concrete_powder")
                .register("minecraft:light_blue_concrete_powder")
                .register("minecraft:yellow_concrete_powder")
                .register("minecraft:lime_concrete_powder")
                .register("minecraft:pink_concrete_powder")
                .register("minecraft:gray_concrete_powder")
                .register("minecraft:light_gray_concrete_powder")
                .register("minecraft:cyan_concrete_powder")
                .register("minecraft:purple_concrete_powder")
                .register("minecraft:blue_concrete_powder")
                .register("minecraft:brown_concrete_powder")
                .register("minecraft:green_concrete_powder")
                .register("minecraft:red_concrete_powder")
                .register("minecraft:black_concrete_powder")
        ;
    }


    public static MaterialType getBlockMaterial(String blockLocation, Stream<String> blockTags) {
        MaterialType type = BLOCK_MATERIALS.get(blockLocation);
        if (type != null)
            return type;

        return blockTags.map(TAG_MATERIALS::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(MaterialType.AMBIGUOUS);
    }


    public static RegisterHelper registerTags(MaterialType type) {
        return new RegisterHelper(TAG_MATERIALS, type);
    }

    public static RegisterHelper registerBlocks(MaterialType type) {
        return new RegisterHelper(BLOCK_MATERIALS, type);
    }


    public record RegisterHelper(HashMap<String, MaterialType> map, MaterialType type) {
        public RegisterHelper register(String location) {
            map.putIfAbsent(location, type);
            return this;
        }
    }
}
