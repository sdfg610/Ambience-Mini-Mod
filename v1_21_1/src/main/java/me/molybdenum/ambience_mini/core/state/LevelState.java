package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.tags.AmTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
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
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LevelState extends BaseLevelState<BlockPos, Vec3, BlockState, Entity> 
{
    private final Minecraft mc = Minecraft.getInstance();
    private ClientLevel level = null;


    @Override
    public boolean isNull() {
        return level == null;
    }

    @Override
    public boolean notNull() {
        return level != null;
    }


    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        ClientLevel newLevel = mc.level;
        if (level != newLevel) {
            if (messages != null)
                messages.add("Level instance changed from '" + getLevelString(level) + "' to '" + getLevelString(newLevel) + "' since last update.");
            level = newLevel;
        }
    }

    private String getLevelString(ClientLevel lv) {
        return lv == null ? "null" : lv.dimension().location() + "/" + lv.hashCode();
    }


    @Override
    public boolean isWorldTickingPaused() {
        IntegratedServer srv = Minecraft.getInstance().getSingleplayerServer();
        return srv != null && (Boolean) ObfuscationReflectionHelper.getPrivateValue(IntegratedServer.class, srv, "paused");
    }


    @Override
    public String getDimensionID() {
        assert level != null;
        return level.dimension().location().toString();
    }

    @Override
    public String getBiomeID(BlockPos blockPos) {
        assert level != null;
        return level.getBiome(blockPos).unwrap().map(
                (resourceKey) -> resourceKey.location().toString(),
                (biome) -> ""
        );
    }

    @Override
    public List<String> getBiomeTagIDs(BlockPos blockPos) {
        assert level != null;
        return level.getBiome(blockPos)
                .tags()
                .map(tag -> tag.location().toString())
                .toList();
    }


    @Override
    public int getTime() {
        assert level != null;
        return (int) level.getDayTime();
    }


    @Override
    public boolean isRaining() {
        assert level != null;
        return level.isRaining();
    }

    @Override
    public boolean isThundering() {
        assert level != null;
        return level.isThundering();
    }

    @Override
    public boolean isColdEnoughToSnow(BlockPos blockPos) {
        assert level != null;
        return level.getBiome(blockPos).value().coldEnoughToSnow(blockPos);
    }


    @Override
    public Entity getEntityById(int id) {
        assert level != null;
        return level.getEntity(id);
    }

    @Override
    public Vec3 getEntityPosition(Entity entity) {
        return entity.position();
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
        assert level != null;
        return level.getBrightness(LightLayer.SKY, blockPos);
    }

    @Override
    public int getBlockLightAt(BlockPos blockPos) {
        assert level != null;
        return level.getBrightness(LightLayer.BLOCK, blockPos);
    }


    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        assert level != null;
        return level.getBlockState(blockPos);
    }

    @Override
    public BlockPos getNearestBlockOrFurthestAir(Vec3 from, Vec3 to) {
        assert level != null;
        BlockHitResult hit = level.clip(new ClipContext(
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
        assert level != null;
        var area = new AABB(
                center.getX() - horizontalRadius, center.getY() - verticalRadius, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + verticalRadius, center.getZ() + horizontalRadius
        );
        return level.getEntitiesOfClass(clazz, area, ignore -> true);
    }
}
