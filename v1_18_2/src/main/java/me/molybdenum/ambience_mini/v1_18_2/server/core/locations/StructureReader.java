package me.molybdenum.ambience_mini.v1_18_2.server.core.locations;

import me.molybdenum.ambience_mini.engine.server.core.locations.BaseStructureReader;
import me.molybdenum.ambience_mini.engine.shared.core.structures.AmStructure;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class StructureReader extends BaseStructureReader<ServerPlayer, Level, StructureStart> {
    private final Registry<ConfiguredStructureFeature<?,?>> registry;


    public StructureReader(MinecraftServer server) {
        this.registry = server.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
    }


    @Override
    protected Pair<Level, Vector2i> getLevelAndChunkPos(ServerPlayer serverPlayer) {
        var playerPos = serverPlayer.blockPosition();
        return new Pair<>(
                serverPlayer.getLevel(),
                new Vector2i(playerPos.getX(), playerPos.getZ()).toChunkPos()
        );
    }

    @Override
    protected List<Vector2i> getReferencedPositionsFromChunkPos(Level level, Vector2i chunkPos) {
        var chunks = new ArrayList<Vector2i>();
        level.getChunk(chunkPos.x(), chunkPos.y()).getAllReferences().values().forEach(longs ->
                longs.forEach(i -> {
                    var pos = new ChunkPos(i);
                    chunks.add(new Vector2i(pos.x, pos.z));
                })
        );
        return chunks;
    }

    @Override
    protected Collection<StructureStart> getStructuresFromChunkPos(Level level, Vector2i chunkPos) {
        return level.getChunk(chunkPos.x(), chunkPos.y())
                .getAllStarts()
                .values();
    }


    @Override
    protected String getDimensionId(Level level) {
        return level.dimension().location().toString();
    }

    @Override
    protected String getStructureId(StructureStart structure) {
        var key = registry.getKey(structure.getFeature());
        return key == null ? "UNKNOWN" : key.toString();
    }

    @Override
    protected Boolean isSurfaceStructure(StructureStart structure) {
        return structure.getFeature().feature.step() == GenerationStep.Decoration.SURFACE_STRUCTURES;
    }


    @Override
    protected Stream<AmStructure.Piece> getPieces(StructureStart structure) {
        return structure.getPieces().stream()
                .map(piece -> toPiece(piece.getBoundingBox()));
    }

    private AmStructure.Piece toPiece(BoundingBox box) {
        return new AmStructure.Piece(
                new Vector3i(box.minX(), box.minY(), box.minZ()),
                new Vector3i(box.maxX(), box.maxY(), box.maxZ())
        );
    }
}
