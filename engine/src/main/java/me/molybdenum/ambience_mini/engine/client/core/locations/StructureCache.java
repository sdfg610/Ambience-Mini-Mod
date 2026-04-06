package me.molybdenum.ambience_mini.engine.client.core.locations;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.GetStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.structures.AmStructure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StructureCache {
    private final Map<String, SubCache> dimensionToCache = new ConcurrentHashMap<>();

    private BaseClientNetworkManager network;
    private ServerSetup serverSetup;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.network != null)
            throw new RuntimeException("Multiple calls to 'StructureCache.init'!");

        this.network = core.networkManager;
        this.serverSetup = core.serverSetup;
    }


    public List<String> getIntersectingStructures(String dimension, Vector3i position) {
        if (!serverSetup.serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0))
            return null;

        return getSubCache(dimension).getStructuresAt(position).map(amStructures ->
                Arrays.stream(amStructures)
                .filter(struct -> struct.containsPosition(position))
                .map(struct -> struct.name)
                .toList()
        ).orElse(null);
    }


    public void setReferences(String dimension, Map<Vector2i, List<Vector2i>> chunkToReferences) {
        getSubCache(dimension).setReferences(chunkToReferences);
    }


    public void setStructures(String dimension, Vector2i chunk, List<AmStructure> structures) {
        AmStructure[] structs = new AmStructure[structures.size()];
        structures.toArray(structs);
        getSubCache(dimension).setStructures(chunk, structs);
    }


    public void clear() {
        dimensionToCache.clear();
    }


    private SubCache getSubCache(String dimension) {
        return dimensionToCache.computeIfAbsent(dimension, key -> new SubCache());
    }


    class SubCache {

        private static final long FETCH_TIMEOUT = 1000;
        private static final int CACHE_SIZE = 10;

        private static final int FETCH_RADIUS = 2;
        private long lastFetchTime = 0;

        private int cacheIndex;

        @SuppressWarnings("unchecked")
        private final Pair<Vector2i, AmStructure[]>[] chunkToStructuresCache = new Pair[CACHE_SIZE]; // Keep 'CACHE_SIZE' of the most recent chunk-structure-lists loaded for speed.

        private final Map<Vector2i, AmStructure[][]> regionToStructures = new ConcurrentHashMap<>(); // Structures start in one specific chunk but may touch multiple chunks.
        private final Map<Vector2i, Vector2i[][]> regionToReferences = new ConcurrentHashMap<>(); // Other chunks references the chunk in which a structure starts.


        protected Optional<AmStructure[]> getStructuresAt(Vector3i position) {
            var chunkPos = position.toChunkPos();
            var cache = getFromCache(chunkPos);
            if (cache != null)
                return Optional.of(cache);

            requestMissingReferences(chunkPos);

            Vector2i[] references = getReferences(chunkPos);
            if (references == null)
                return Optional.empty();

            ArrayList<AmStructure> allStructures = new ArrayList<>();
            for (var mainChunkPos : references) {
                var structures = getStructures(mainChunkPos);
                if (structures == null)
                    return Optional.empty();
                allStructures.addAll(Arrays.asList(structures));
            }

            return Optional.of(putToCacheAndGet(chunkPos, allStructures));
        }


        private AmStructure[] getFromCache(Vector2i chunkPos) {
            for (var pair : chunkToStructuresCache) {
                if (pair != null && pair.left().equals(chunkPos))
                    return pair.right();
            }
            return null;
        }

        private AmStructure[] putToCacheAndGet(Vector2i chunkPos, List<AmStructure> structures) {
            AmStructure[] structuresArray = new AmStructure[structures.size()];
            structures.toArray(structuresArray);
            chunkToStructuresCache[(cacheIndex++) % CACHE_SIZE] = new Pair<>(chunkPos, structuresArray);
            return structuresArray;
        }


        protected Vector2i[] getReferences(Vector2i chunkPos) {
            return getReferenceRegion(chunkPos)[getChunkRegionIndex(chunkPos)];
        }

        protected void setReferences(Map<Vector2i, List<Vector2i>> chunkToReferences) {
            for (var entry : chunkToReferences.entrySet()) {
                var chunkPos = entry.getKey();
                var references = entry.getValue();

                Vector2i[] refs = new Vector2i[references.size()];
                references.toArray(refs);
                getReferenceRegion(chunkPos)[getChunkRegionIndex(chunkPos)] = refs;
                requestMissingStructures(refs);
            }
            lastFetchTime = 0L;
        }

        private Vector2i[][] getReferenceRegion(Vector2i chunkPos) {
            return regionToReferences.computeIfAbsent(chunkPos.toRegionPos(), key -> new Vector2i[32*32][]);
        }


        protected AmStructure[] getStructures(Vector2i chunkPos) {
            return getStructureRegion(chunkPos)[getChunkRegionIndex(chunkPos)];
        }

        protected void setStructures(Vector2i chunkPos, AmStructure[] structures) {
            getStructureRegion(chunkPos)[getChunkRegionIndex(chunkPos)] = structures;
        }

        private AmStructure[][] getStructureRegion(Vector2i chunkPos) {
            return regionToStructures.computeIfAbsent(chunkPos.toRegionPos(), key -> new AmStructure[32*32][]);
        }


        private int getChunkRegionIndex(Vector2i chunkPos) {
            var regionChunkPos = chunkPos.toRegionChunkPos();
            return 32*regionChunkPos.x() + regionChunkPos.y();
        }


        private void requestMissingReferences(Vector2i centerChunk) {
            long now = System.currentTimeMillis();
            if (now - lastFetchTime > FETCH_TIMEOUT) {
                lastFetchTime = now;
                ArrayList<Vector2i> chunks = new ArrayList<>();

                int min = -FETCH_RADIUS + 1;
                for (int dx = min; dx < FETCH_RADIUS; dx++)
                    for (int dy = min; dy < FETCH_RADIUS; dy++) {
                        Vector2i chunkPos = centerChunk.offset(dx, dy);
                        if (getReferences(chunkPos) == null)
                            chunks.add(chunkPos);
                    }

                if (!chunks.isEmpty())
                    network.sendToServer(new GetStructuresMessage(true, chunks));
            }
        }

        private void requestMissingStructures(Vector2i[] references) {
            ArrayList<Vector2i> chunks = new ArrayList<>();

            for (var reference : references)
                if (getStructures(reference) == null)
                    chunks.add(reference);

            if (!chunks.isEmpty())
                network.sendToServer(new GetStructuresMessage(false, chunks));
        }
    }
}
