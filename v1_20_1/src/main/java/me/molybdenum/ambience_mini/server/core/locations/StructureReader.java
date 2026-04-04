package me.molybdenum.ambience_mini.server.core.locations;

import me.molybdenum.ambience_mini.engine.server.core.locations.BaseStructureReader;
import me.molybdenum.ambience_mini.engine.shared.structures.AmStructure;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StructureReader extends BaseStructureReader<ServerPlayer, LevelChunk, StructureStart> {
    private final Registry<Structure> registry;


    public StructureReader(MinecraftServer server) {
        this.registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
    }


    @Override
    protected Pair<String, LevelChunk> getDimensionIdAndChunk(ServerPlayer serverPlayer) {
        var level = serverPlayer.level();
        return new Pair<>(
                level.dimension().location().toString(),
                level.getChunkAt(serverPlayer.blockPosition())
        );
    }

    @Override
    protected List<LevelChunk> getReferencedChunksFromChunk(LevelChunk levelChunk) {
        var level = levelChunk.getLevel();
        var chunks = new ArrayList<LevelChunk>();
        levelChunk.getAllReferences().values().forEach(longs ->
                longs.forEach(i ->
                        chunks.add(level.getChunkAt(new ChunkPos(i).getWorldPosition()))
                )
        );
        return chunks;
    }

    @Override
    protected List<Vector2i> getReferencedPositionsFromChunk(LevelChunk levelChunk) {
        var positions = new ArrayList<Vector2i>();
        levelChunk.getAllReferences().values().forEach(longs ->
                longs.forEach(i -> {
                    var pos = new ChunkPos(i);
                    positions.add(new Vector2i(pos.x, pos.z));
                })
        );
        return positions;
    }

    @Override
    protected Collection<StructureStart> getStructuresFromChunk(LevelChunk levelChunk) {
        return levelChunk.getAllStarts().values();
    }

    @Override
    protected Vector2i getPosition(LevelChunk levelChunk) {
        var pos = levelChunk.getPos();
        return new Vector2i(pos.x, pos.z);
    }

    @Override
    protected String getId(StructureStart structure) {
        var key = registry.getKey(structure.getStructure());
        return key == null ? "UNKNOWN" : key.toString();
    }

    @Override
    protected List<AmStructure.Piece> getPieces(StructureStart structure) {
        return structure.getPieces().stream()
                .map(piece -> toPiece(piece.getBoundingBox()))
                .toList();
    }

    private AmStructure.Piece toPiece(BoundingBox box) {
        return new AmStructure.Piece(
                new Vector3i(box.minX(), box.minY(), box.minZ()),
                new Vector3i(box.maxX(), box.maxY(), box.maxZ())
        );
    }
}
