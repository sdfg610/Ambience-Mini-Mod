package me.molybdenum.ambience_mini.engine.server.core.locations;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.PutChunkReferenceMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.PutChunkStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.structures.AmStructure;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class BaseStructureReader<TServerPlayer, TChunk, TStructureStart>
{
    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract Pair<String, TChunk> getDimensionIdAndChunk(TServerPlayer player);
    protected abstract List<TChunk> getReferencedChunksFromChunk(TChunk chunk);
    protected abstract List<Vector2i> getReferencedPositionsFromChunk(TChunk chunk);
    protected abstract Collection<TStructureStart> getStructuresFromChunk(TChunk chunk);

    protected abstract Vector2i getPosition(TChunk chunk);
    protected abstract String getId(TStructureStart structure);
    protected abstract List<AmStructure.Piece> getPieces(TStructureStart structure);


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public PutChunkReferenceMessage getReferences(TServerPlayer player) {
        var idAndChunk = getDimensionIdAndChunk(player);
        var id = idAndChunk.left();
        var chunk = idAndChunk.right();

        return new PutChunkReferenceMessage(
                id,
                getPosition(chunk),
                getReferencedPositionsFromChunk(chunk)
        );
    }

    public PutChunkStructuresMessage getStructures(TServerPlayer player) {
        var idAndChunk = getDimensionIdAndChunk(player);
        var id = idAndChunk.left();
        var chunk = idAndChunk.right();

        HashMap<Vector2i, List<AmStructure>> chunksToStructures = new HashMap<>();
        getReferencedChunksFromChunk(chunk).forEach(
                startChunk -> chunksToStructures.put(
                        getPosition(startChunk),
                        getStructuresFromChunk(startChunk).stream()
                                .map(struct -> new AmStructure(getId(struct), getPieces(struct)))
                                .toList()
                )
        );

        return new PutChunkStructuresMessage(id, chunksToStructures);
    }
}
