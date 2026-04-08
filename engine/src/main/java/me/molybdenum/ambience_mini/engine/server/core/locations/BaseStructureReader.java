package me.molybdenum.ambience_mini.engine.server.core.locations;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.PutChunkReferencesMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.PutChunkStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.structures.AmStructure;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public abstract class BaseStructureReader<TServerPlayer, TLevel, TStructureStart>
{
    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract Pair<TLevel, Vector2i> getLevelAndChunkPos(TServerPlayer player);

    protected abstract List<Vector2i> getReferencedPositionsFromChunkPos(TLevel level, Vector2i chunkPos);
    protected abstract Collection<TStructureStart> getStructuresFromChunkPos(TLevel level, Vector2i chunkPos);

    protected abstract Boolean isSurfaceStructure(TStructureStart structure);
    protected abstract String getDimensionId(TLevel level);
    protected abstract String getStructureId(TStructureStart structure);
    protected abstract Stream<AmStructure.Piece> getPieces(TStructureStart structure);


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public PutChunkReferencesMessage getReferences(TServerPlayer player, List<Vector2i> chunksToFetch) {
        var pair = getLevelAndChunkPos(player);
        var level = pair.left();
        var serverChunkPos = pair.right();

        HashMap<Vector2i, List<Vector2i>> chunksToReferences = new HashMap<>();
        for (var chunkPos : chunksToFetch)
            if (chunkPos.distanceTo(serverChunkPos) <= 10) // Ensure one cannot just probe the entire world.
                chunksToReferences.put(chunkPos, getReferencedPositionsFromChunkPos(level, chunkPos));

        return new PutChunkReferencesMessage(getDimensionId(level), chunksToReferences);
    }

    public PutChunkStructuresMessage getStructures(TServerPlayer player, List<Vector2i> chunksToFetch) {
        var pair = getLevelAndChunkPos(player);
        var level = pair.left();
        var serverChunkPos = pair.right();

        HashMap<Vector2i, List<AmStructure>> chunksToStructures = new HashMap<>();
        for (var chunkPos : chunksToFetch)
            if (chunkPos.distanceTo(serverChunkPos) <= 15) // Ensure one cannot just probe the entire world.
                if (!chunksToStructures.containsKey(chunkPos)) // Don't do duplicate work.
                    chunksToStructures.put(
                            chunkPos,
                            getAndProcessPieces(level, chunkPos)
                            );


        return new PutChunkStructuresMessage(getDimensionId(level), chunksToStructures);
    }


    private List<AmStructure> getAndProcessPieces(TLevel level, Vector2i chunkPos) {
        return getStructuresFromChunkPos(level, chunkPos).stream()
                .map(struct -> {
                    boolean isSurface = isSurfaceStructure(struct);
                    var pieces = getPieces(struct)
                            .map(piece ->
                                    new AmStructure.Piece(  // Give all hitboxes a bit of padding. Overworld structures get a lot more height.
                                            piece.min().offset(-2,-2, -2),
                                            piece.max().offset(
                                                    2,
                                                    isSurface ? 10 : 2,
                                                    2
                                            )
                                    )
                            ).toList();
                    return new AmStructure(getStructureId(struct), pieces);
                })
                .toList();
    }
}
