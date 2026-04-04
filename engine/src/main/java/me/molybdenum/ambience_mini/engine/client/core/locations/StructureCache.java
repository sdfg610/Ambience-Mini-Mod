package me.molybdenum.ambience_mini.engine.client.core.locations;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.GetStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.structures.AmStructure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StructureCache {
    private final Map<String, SubCache> dimensionToCache = new ConcurrentHashMap<>();

    private BaseClientNetworkManager network;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        this.network = core.networkManager;
    }


    public List<String> getIntersectingStructures(String dimension, Vector3i position) {
        var structures = getSubCache(dimension).getStructuresAt(position);

        return Arrays.stream(structures)
                .filter(struct -> struct.containsPosition(position))
                .map(struct -> struct.name)
                .toList();
    }


    public void setReferences(String dimension, Vector2i chunkPos, List<Vector2i> references) {
        Vector2i[] refs = new Vector2i[references.size()];
        references.toArray(refs);
        getSubCache(dimension).setReferences(chunkPos, refs);
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
        private static final AmStructure[] NO_STRUCTS = {};

        private long lastFetchTime = 0;
        private int cacheIndex;

        @SuppressWarnings("unchecked")
        private final Pair<Vector2i, AmStructure[]>[] chunkToStructuresCache = new Pair[CACHE_SIZE]; // Keep 'CACHE_SIZE' of the most recent chunk-structure-lists loaded for speed.

        private final Map<Vector2i, AmStructure[][]> regionToStructures = new ConcurrentHashMap<>(); // Structures start in one specific chunk but may touch multiple chunks.
        private final Map<Vector2i, Vector2i[][]> regionToReferences = new ConcurrentHashMap<>(); // Other chunks references the chunk in which a structure starts.


        protected AmStructure[] getStructuresAt(Vector3i position) {
            var chunkPos = position.toChunkPos();
            var cache = getFromCache(chunkPos);
            if (cache != null)
                return cache;

            Vector2i[] references = getReferences(chunkPos);
            if (references == null) {
                tryFetchReferences();
                return NO_STRUCTS;
            }

            ArrayList<AmStructure> allStructures = new ArrayList<>();
            for (var mainChunkPos : references) {
                var structures = getStructures(mainChunkPos);
                if (structures == null) {
                    tryFetchStructures();
                    return NO_STRUCTS;
                }
                allStructures.addAll(Arrays.asList(structures));
            }

            return putToCacheAndGet(chunkPos, allStructures);
        }


        private AmStructure[] getFromCache(Vector2i chunkPos) {
            for (var pair : chunkToStructuresCache) {
                if (pair != null && pair.left().equals(chunkPos))
                    return pair.right();
            }
            return null;
        }

        private AmStructure[] putToCacheAndGet(Vector2i chunk, List<AmStructure> structures) {
            AmStructure[] structuresArray = new AmStructure[structures.size()];
            structures.toArray(structuresArray);
            chunkToStructuresCache[(cacheIndex++) % CACHE_SIZE] = new Pair<>(chunk, structuresArray);
            return structuresArray;
        }


        protected Vector2i[] getReferences(Vector2i chunkPos) {
            return getReferenceRegion(chunkPos)[getChunkRegionIndex(chunkPos)];
        }

        protected void setReferences(Vector2i chunkPos, Vector2i[] references) {
            getReferenceRegion(chunkPos)[getChunkRegionIndex(chunkPos)] = references;
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
            lastFetchTime = 0L;
        }

        private AmStructure[][] getStructureRegion(Vector2i chunkPos) {
            return regionToStructures.computeIfAbsent(chunkPos.toRegionPos(), key -> new AmStructure[32*32][]);
        }


        private int getChunkRegionIndex(Vector2i chunkPos) {
            var regionChunkPos = chunkPos.toRegionChunkPos();
            return 32*regionChunkPos.x() + regionChunkPos.y();
        }


        private void tryFetchReferences() {
            tryFetch(true);
        }

        private void tryFetchStructures() {
            tryFetch(false);
        }

        private void tryFetch(boolean getReferences) {
            long now = System.currentTimeMillis();
            if (now - lastFetchTime > FETCH_TIMEOUT) { // Wait for 'FETCH_TIMEOUT' milliseconds before re-issuing a fetch request.
                lastFetchTime = now;
                network.sendToServer(new GetStructuresMessage(getReferences));
            }
        }
    }
}
