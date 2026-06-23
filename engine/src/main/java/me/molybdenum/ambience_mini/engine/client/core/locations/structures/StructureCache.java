package me.molybdenum.ambience_mini.engine.client.core.locations.structures;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.GetStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.core.structures.AmStructure;

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

        return getSubCache(dimension).getStructureNamesAt(position);
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


    class SubCache
    {
        private static final long FETCH_TIMEOUT = 1000;
        private static final int FETCH_RADIUS = 2;
        private long lastFetchTime = 0;

        private final Map<RegionPos, ArrayList<AmStructure>> regionToStructures = new ConcurrentHashMap<>();  // Structures start in one specific chunk but may touch multiple chunks.
        private final Map<RegionPos, BitSet> regionToFetchedReferenceChunks = new ConcurrentHashMap<>();  // Whether chunk references have been fetched for some chunk
        private final Map<RegionPos, BitSet> regionToFetchedStructureChunks = new ConcurrentHashMap<>();  // Whether chunk structures have been fetched for some chunk



        protected List<String> getStructureNamesAt(Vector3i position) {
            ChunkPos chunkPos = position.toChunkPos();

            requestMissingReferencesAndStructures(chunkPos);

            ArrayList<String> intersections = new ArrayList<>();
            for (var struct : getStructuresForRegion(chunkPos.getRegionPos()))
                if (struct.containsPosition(position))
                    intersections.add(struct.name);

            return intersections;
        }

        private ArrayList<AmStructure> getStructuresForRegion(RegionPos regionPos) {
            return regionToStructures.computeIfAbsent(regionPos, ignored -> new ArrayList<>());
        }


        protected void setReferences(Map<Vector2i, List<Vector2i>> chunkToReferences) {
            ArrayList<Vector2i> chunksToFetch = new ArrayList<>();
            for (var entry : chunkToReferences.entrySet()) {
                var chunkPos = new ChunkPos(entry.getKey());
                getRegionReferenceBitSet(chunkPos.getRegionPos()).set(chunkPos.getRegionChunkIndex());

                for (var reference : entry.getValue())
                    if (!chunksToFetch.contains(reference))
                        chunksToFetch.add(reference);
            }
            requestMissingStructures(chunksToFetch);
            lastFetchTime = 0L;
        }

        private boolean hasLoadedReferencesFor(ChunkPos chunkPos) {
            return getRegionReferenceBitSet(chunkPos.getRegionPos()).get(chunkPos.getRegionChunkIndex());
        }

        private BitSet getRegionReferenceBitSet(RegionPos regionPos) {
            return regionToFetchedReferenceChunks.computeIfAbsent(regionPos, key -> new BitSet(32*32));
        }


        protected void setStructures(Vector2i chunkPos, AmStructure[] structures) {
            var realPos = new ChunkPos(chunkPos);
            getRegionStructureBitSet(realPos.getRegionPos()).set(realPos.getRegionChunkIndex());

            for (var struct : structures)
                for (var regionPos : struct.getIntersectingRegions())
                    getStructuresForRegion(regionPos).add(struct);
        }

        protected boolean hasLoadedStructuresFor(ChunkPos chunkPos) {
            return getRegionStructureBitSet(chunkPos.getRegionPos()).get(chunkPos.getRegionChunkIndex());
        }

        private BitSet getRegionStructureBitSet(RegionPos regionPos) {
            return regionToFetchedStructureChunks.computeIfAbsent(regionPos, key -> new BitSet(32*32));
        }


        private void requestMissingReferencesAndStructures(ChunkPos centerChunk) {
            long now = System.currentTimeMillis();
            if (now - lastFetchTime > FETCH_TIMEOUT) {
                lastFetchTime = now;
                ArrayList<Vector2i> chunks = new ArrayList<>();

                int min = -FETCH_RADIUS + 1;
                for (int dx = min; dx < FETCH_RADIUS; dx++)
                    for (int dy = min; dy < FETCH_RADIUS; dy++) {
                        ChunkPos chunkPos = centerChunk.offset(dx, dy);
                        if (!hasLoadedReferencesFor(chunkPos))
                            chunks.add(chunkPos.asVector2i());
                    }

                if (!chunks.isEmpty())
                    network.sendToServer(new GetStructuresMessage(true, chunks));
            }
        }

        private void requestMissingStructures(List<Vector2i> references) {
            List<Vector2i> chunks = references.stream()
                    .filter(ref -> !hasLoadedStructuresFor(new ChunkPos(ref)))
                    .distinct()
                    .toList();

            if (!chunks.isEmpty())
                network.sendToServer(new GetStructuresMessage(false, chunks));
        }
    }
}
