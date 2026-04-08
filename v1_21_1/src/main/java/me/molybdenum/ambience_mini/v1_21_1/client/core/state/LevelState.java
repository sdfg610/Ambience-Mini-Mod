package me.molybdenum.ambience_mini.v1_21_1.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.warden.Warden;
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

import java.util.List;
import java.util.stream.Stream;


public class LevelState extends BaseLevelState<BlockPos, Vec3, BlockState, Entity, ClientLevel>
{
    private final Minecraft mc = Minecraft.getInstance();


    @Override
    protected ClientLevel getCurrentLevel() {
        return mc.level;
    }

    @Override
    protected String getLevelString(ClientLevel lv) {
        return lv == null ? "null" : lv.dimension().location() + "/" + lv.hashCode();
    }


    @Override
    public Boolean isWorldTickingPaused() {
        IntegratedServer srv = Minecraft.getInstance().getSingleplayerServer();
        return srv == null ? null : ObfuscationReflectionHelper.getPrivateValue(IntegratedServer.class, srv, "paused");
    }

    @Override
    public String getDifficulty() {
        if (cachedLevel == null)
            return null;
        return cachedLevel.getLevelData().isHardcore()
                ? "hardcore"
                : cachedLevel.getLevelData().getDifficulty().getKey();
    }


    @Override
    public String getDimensionID() {
        return cachedLevel == null ? null : cachedLevel.dimension().location().toString();
    }

    @Override
    public String getBiomeID(BlockPos blockPos) {
        return cachedLevel == null ? null : cachedLevel.getBiome(blockPos).unwrap().map(
                (resourceKey) -> resourceKey.location().toString(),
                (biome) -> ""
        );
    }

    @Override
    public List<String> getBiomeTagIDs(BlockPos blockPos) {
        return cachedLevel == null ? null : cachedLevel.getBiome(blockPos)
                .tags()
                .map(tag -> tag.location().toString())
                .toList();
    }


    @Override
    public Integer getTime() {
        return cachedLevel == null ? null : (int)cachedLevel.getDayTime();
    }


    @Override
    public Boolean isRaining() {
        return cachedLevel == null ? null : cachedLevel.isRaining();
    }

    @Override
    public Boolean isThundering() {
        return cachedLevel == null ? null : cachedLevel.isThundering();
    }

    @Override
    public Boolean isColdEnoughToSnow(BlockPos blockPos) {
        return cachedLevel == null ? null : cachedLevel.getBiome(blockPos).value().coldEnoughToSnow(blockPos);
    }


    @Override
    public Entity getEntityById(int id) {
        return cachedLevel == null ? null : cachedLevel.getEntity(id);
    }

    @Override
    public Vec3 getEntityPosition(Entity entity) {
        return entity.position();
    }


    @Override
    public Integer countNearbyVillagers(BlockPos center, int horizontalRadius, int verticalRadius) {
        return cachedLevel == null ? null : getNearbyEntities(Villager.class, center, horizontalRadius, verticalRadius).size();
    }

    @Override
    public Integer countNearbyAnimals(BlockPos center, int horizontalRadius, int verticalRadius) {
        return cachedLevel == null ? null : getNearbyEntities(Animal.class, center, horizontalRadius, verticalRadius).size();
    }

    @Override
    public Double shortestDistanceToWarden(Vec3 position, int cubeSearchRadius) {
        if (cachedLevel == null)
            return null;

        var min = getNearbyEntities(Warden.class, BlockPos.containing(position), cubeSearchRadius, cubeSearchRadius).stream()
                .mapToDouble(warden -> warden.getEyePosition().distanceTo(position))
                .min();
        return min.isPresent() ? min.getAsDouble() : null;
    }


    @Override
    public Integer getMaxSkyLightAt(BlockPos blockPos) {
        return cachedLevel == null ? null : cachedLevel.getBrightness(LightLayer.SKY, blockPos);
    }

    @Override
    public Integer getBlockLightAt(BlockPos blockPos) {
        return cachedLevel == null ? null : cachedLevel.getBrightness(LightLayer.BLOCK, blockPos);
    }


    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return cachedLevel == null ? null : cachedLevel.getBlockState(blockPos);
    }

    @Override
    public Object getBlock(BlockState blockState) {
        return blockState.getBlock();
    }

    @Override
    public String getBlockId(BlockState blockState) {
        ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        return loc.toString();
    }

    @Override
    public Stream<String> getBlockTags(BlockState blockState) {
        return blockState.getTags().map(tag -> tag.location().toString());
    }

    @Override
    public BlockPos getNearestBlockOrFurthestAir(Vec3 from, Vec3 to) {
        if (cachedLevel == null)
            return null;

        BlockHitResult hit = cachedLevel.clip(new ClipContext(
                from, to,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                CollisionContext.empty()
        ));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : vectorToBlockPos(to);
    }

    @Override
    public boolean isAir(BlockState blockState) {
        return blockState.isAir();
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

    @Override
    @Nullable
    public Vector3i toAmVector3i(@Nullable BlockPos blockPos) {
        return blockPos == null ? null : new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    @Nullable
    public Vector3d toAmVector3d(@Nullable Vec3 position) {
        return position == null ? null : new Vector3d(position.x, position.y, position.z);
    }


    // ------------------------------------------------------------------------------------------------
    // Utilities
    private <T extends Entity> List<T> getNearbyEntities(Class<T> clazz, BlockPos center, int horizontalRadius, int verticalRadius)
    {
        assert cachedLevel != null;
        var area = new AABB(
                center.getX() - horizontalRadius, center.getY() - verticalRadius, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + verticalRadius, center.getZ() + horizontalRadius
        );
        return cachedLevel.getEntitiesOfClass(clazz, area, ignore -> true);
    }
}
