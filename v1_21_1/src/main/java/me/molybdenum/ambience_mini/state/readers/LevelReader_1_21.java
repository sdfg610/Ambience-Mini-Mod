package me.molybdenum.ambience_mini.state.readers;

import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.setup.AmTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

public class LevelReader_1_21 extends BaseLevelReader<BlockPos, Vec3, BlockState> {
    private final Minecraft mc = Minecraft.getInstance();


    @Override
    public boolean isNull() {
        return mc.level == null;
    }

    @Override
    public boolean notNull() {
        return mc.level != null;
    }


    @Override
    public String getDimensionId() {
        assert mc.level != null;
        return mc.level.dimension().location().toString();
    }

    @Override
    public String getBiomeID(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBiome(blockPos).unwrap().map(
                (resourceKey) -> resourceKey.location().toString(),
                (biome) -> ""
        );
    }


    @Override
    public int getTime() {
        assert mc.level != null;
        return (int) mc.level.getDayTime();
    }


    @Override
    public boolean isRaining() {
        assert mc.level != null;
        return mc.level.isRaining();
    }

    @Override
    public boolean isThundering() {
        assert mc.level != null;
        return mc.level.isThundering();
    }

    @Override
    public boolean isColdEnoughToSnow(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBiome(blockPos).value().coldEnoughToSnow(blockPos);
    }


    @Override
    public int countNearbyVillagers(BlockPos center, int horizontalRadius, int verticalRadius) {
        return getNearbyEntities(Villager.class, center, horizontalRadius, verticalRadius).size();
    }

    @Override
    public int countNearbyAnimals(BlockPos center, int horizontalRadius, int verticalRadius) {
        return getNearbyEntities(Animal.class, center, horizontalRadius, verticalRadius).size();
    }


    @Override
    public int getMaxSkyLightAt(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBrightness(LightLayer.SKY, blockPos);
    }

    @Override
    public int getBlockLightAt(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBrightness(LightLayer.BLOCK, blockPos);
    }


    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBlockState(blockPos);
    }

    @Override
    public BlockPos getNearestBlockOrFurthestAir(Vec3 from, Vec3 to) {
        assert mc.level != null;
        BlockHitResult hit = mc.level.clip(new ClipContext(
                from, to,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
        ));

        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : vectorToBlockPos(to);
    }

    @Override
    public boolean isAir(BlockState blockState) {
        return blockState.isAir();
    }

    @Override
    public boolean isCaveMaterial(BlockState blockState) {
        return blockState.is(AmTags.CAVE_MATERIAL);
    }

    @Override
    public boolean isWeakCaveMaterial(BlockState blockState) {
        return blockState.is(AmTags.WEAK_CAVE_MATERIAL);
    }

    @Override
    public boolean isWeakNonCaveMaterial(BlockState blockState) {
        return blockState.is(AmTags.WEAK_NON_CAVE_MATERIAL);
    }

    @Override
    public boolean isNonCaveMaterial(BlockState blockState) {
        return blockState.is(AmTags.NON_CAVE_MATERIAL);
    }


    @Override
    public BlockPos vectorToBlockPos(Vec3 position) {
        return BlockPos.containing(position);
    }

    @Override
    public BlockPos offsetBlockPos(BlockPos blockPos, int x, int y, int z) {
        return blockPos.offset(x, y, z);
    }

    @Override
    public Vec3 offsetVector(Vec3 position, double x, double y, double z) {
        return position.add(x, y, z);
    }



    // ------------------------------------------------------------------------------------------------
    // Utilities
    private <T extends Entity> List<T> getNearbyEntities(Class<T> clazz, BlockPos center, int horizontalRadius, int verticalRadius)
    {
        assert mc.level != null;
        var area = new AABB(
                center.getX() - horizontalRadius, center.getY() - verticalRadius, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + verticalRadius, center.getZ() + horizontalRadius
        );
        return mc.level.getEntitiesOfClass(clazz, area, ignore -> true);
    }
}
