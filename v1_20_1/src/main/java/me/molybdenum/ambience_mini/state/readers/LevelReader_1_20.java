package me.molybdenum.ambience_mini.state.readers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class LevelReader_1_20 implements me.molybdenum.ambience_mini.engine.state.readers.LevelReader<BlockPos, Vec3, BlockState>
{
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
        return false;
    }

    @Override
    public boolean isColdEnoughToSnow(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBiome(blockPos).value().coldEnoughToSnow(blockPos);
    }


    @Override
    public int countNearbyVillagers(BlockPos center, int horizontalRadius, int verticalRadius) {
        return countEntities(Villager.class, center, horizontalRadius, verticalRadius);
    }

    @Override
    public int countNearbyAnimals(BlockPos center, int horizontalRadius, int verticalRadius) {
        return countEntities(Animal.class, center, horizontalRadius, verticalRadius);
    }


    @Override
    public boolean canSeeSky(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.canSeeSky(blockPos);
    }

    @Override
    public int getMaxSkyLight(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBrightness(LightLayer.SKY, blockPos);
    }


    @Override
    public @Nullable BlockPos tryGetNearestBlockInDirection(Vec3 from, Vec3 to) {
        assert mc.level != null;
        var hit = mc.level.clip(new ClipContext(
                from, to,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                null
        ));

        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        assert mc.level != null;
        return mc.level.getBlockState(blockPos);
    }


    // ------------------------------------------------------------------------------------------------
    // Utilities
    private <T extends Entity> int countEntities(Class<T> clazz, BlockPos center, int horizontalRadius, int verticalRadius)
    {
        assert mc.level != null;
        var area = new AABB(
                center.getX() - horizontalRadius, center.getY() - verticalRadius, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + verticalRadius, center.getZ() + horizontalRadius
        );
        return mc.level.getEntitiesOfClass(clazz, area, ignore -> true).size();
    }
}
