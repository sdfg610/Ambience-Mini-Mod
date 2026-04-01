package me.molybdenum.ambience_mini.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.render.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;
import me.molybdenum.ambience_mini.engine.client.core.state.BaseLevelState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class LevelState extends BaseLevelState<BlockPos, Vec3, BlockState, Entity, ClientLevel>
{
    private static final String OBF_INTEGRATED_SERVER_PAUSED = "f_120016_";

    private final Minecraft mc = Minecraft.getInstance();


    @Override
    public boolean isNull() {
        return cachedLevel == null;
    }

    @Override
    public boolean notNull() {
        return cachedLevel != null;
    }


    @Override
    protected ClientLevel getCurrentLevel() {
        return mc.level;
    }

    @Override
    protected String getLevelString(ClientLevel lv) {
        return lv == null ? "null" : lv.dimension().location() + "/" + lv.hashCode();
    }
    

    @Override
    public boolean isWorldTickingPaused() {
        IntegratedServer srv = Minecraft.getInstance().getSingleplayerServer();
        return srv != null && (Boolean) ObfuscationReflectionHelper.getPrivateValue(IntegratedServer.class, srv, OBF_INTEGRATED_SERVER_PAUSED);
    }

    @Override
    public String getDifficulty() {
        assert cachedLevel != null;
        if (cachedLevel.getLevelData().isHardcore())
            return "hardcore";
        else
            return cachedLevel.getLevelData().getDifficulty().getKey();
    }


    @Override
    public String getDimensionID() {
        assert cachedLevel != null;
        return cachedLevel.dimension().location().toString();
    }

    @Override
    public String getBiomeID(BlockPos blockPos) {
        assert cachedLevel != null;
        return cachedLevel.getBiome(blockPos).unwrap().map(
                (resourceKey) -> resourceKey.location().toString(),
                (biome) -> ""
        );
    }

    @Override
    public List<String> getBiomeTagIDs(BlockPos blockPos) {
        assert cachedLevel != null;
        return cachedLevel.getBiome(blockPos)
                .tags()
                .map(tag -> tag.location().toString())
                .toList();
    }


    @Override
    public int getTime() {
        assert cachedLevel != null;
        return (int) cachedLevel.getDayTime();
    }


    @Override
    public boolean isRaining() {
        assert cachedLevel != null;
        return cachedLevel.isRaining();
    }

    @Override
    public boolean isThundering() {
        assert cachedLevel != null;
        return cachedLevel.isThundering();
    }

    @Override
    public boolean isColdEnoughToSnow(BlockPos blockPos) {
        assert cachedLevel != null;
        return cachedLevel.getBiome(blockPos).value().coldEnoughToSnow(blockPos);
    }


    @Override
    public Entity getEntityById(int id) {
        assert cachedLevel != null;
        return cachedLevel.getEntity(id);
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
        assert cachedLevel != null;
        return cachedLevel.getBrightness(LightLayer.SKY, blockPos);
    }

    @Override
    public int getBlockLightAt(BlockPos blockPos) {
        assert cachedLevel != null;
        return cachedLevel.getBrightness(LightLayer.BLOCK, blockPos);
    }


    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        assert cachedLevel != null;
        return cachedLevel.getBlockState(blockPos);
    }

    @Override
    public Object getBlock(BlockState blockState) {
        return blockState.getBlock();
    }

    @Override
    public String getBlockId(BlockState blockState) {
        ResourceLocation loc = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
        return loc == null ? "" : loc.toString();
    }

    @Override
    public Stream<String> getBlockTags(BlockState blockState) {
        return blockState.getTags().map(tag -> tag.location().toString());
    }

    @Override
    public BlockPos getNearestBlockOrFurthestAir(Vec3 from, Vec3 to) {
        assert cachedLevel != null;
        BlockHitResult hit = getClip(from, to);
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : vectorToBlockPos(to);
    }

    @Override
    public BlockPos getAirJustBeforeLookedAtBlockIfInRange(Vec3 from, Vec3 to) {
        assert cachedLevel != null;
        BlockHitResult hit = getClip(from, to);
        return hit.getType() == HitResult.Type.BLOCK ? new BlockPos(hit.getDirection().getNormal().offset(hit.getBlockPos())) : null;
    }

    private @NotNull BlockHitResult getClip(Vec3 from, Vec3 to) {
        return cachedLevel.clip(new ClipContext(
                from, to,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                null
        ));
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
