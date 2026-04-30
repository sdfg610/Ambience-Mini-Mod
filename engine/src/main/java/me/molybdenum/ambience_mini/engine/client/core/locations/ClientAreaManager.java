package me.molybdenum.ambience_mini.engine.client.core.locations;


import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.core.areas.AreaStorage;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.areas.AreaOperation;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.GetAreasMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ClientAreaManager
{
    private BaseClientNetworkManager networkManager;

    private AreaStorage areaStorage;

    private final ArrayList<BiConsumer<Area, AreaOperation>> areaUpdatedListeners = new ArrayList<>();
    private final ConcurrentHashMap<Integer, Area> areas = new ConcurrentHashMap<>();


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.networkManager != null)
            throw new RuntimeException("Multiple calls to 'BaseClientAreaManager.init'!");

        networkManager = core.networkManager;
    }


    public Area getAreaById(int id) {
        return areas.get(id);
    }

    public List<Area> getAreasInDimension(String dimension, boolean onlyLocal) {
        return areas.values().stream()
                .filter(area -> area.dimension.equals(dimension) && (!onlyLocal || area.owner.isLocal()))
                .toList();
    }

    public List<Area> getIntersectingAreas(String dimension, Vector3d position) {
        return areas.values().stream()
                .filter(area -> area.dimension.equals(dimension) && area.contains(position))
                .toList();
    }


    public Optional<String> createLocalArea(Area area) {
        area.id = findFreeLocalId();
        return putArea(area);
    }

    private int findFreeLocalId() {
        synchronized (areas) {
            return Math.min(areas.values().stream().map(area -> area.id).reduce(Integer::min).orElse(0), 0) - 1;
        }
    }


    public Optional<String> putArea(Area area) {
        var error = area.validate();
        if (error.isPresent())
            return error;

        Area oldArea = areas.put(area.id, area);
        if (area.isLocalId() || (oldArea != null && oldArea.isLocalId()))
            saveLocalAreas();

        areaUpdatedListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
        return Optional.empty();
    }

    public void deleteArea(int id) {
        Area oldArea = areas.remove(id);

        if (oldArea != null && oldArea.owner.isLocal())
            saveLocalAreas();

        if (oldArea != null)
            areaUpdatedListeners.forEach(listener -> listener.accept(oldArea, AreaOperation.DELETE));
    }


    public void loadAreas(AreaStorage areaStorage) {
        areas.clear();
        networkManager.sendToServer(new GetAreasMessage());

        this.areaStorage = areaStorage;
        loadLocalAreas();
    }

    public void loadLocalAreas() {
        areaStorage.loadAllAreas().ifPresent(
                list -> list.forEach(area -> {
                    areas.put(area.id, area);
                    areaUpdatedListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
                })
        );
    }

    public void saveLocalAreas() {
        areas.values().stream()
                .map(area -> area.dimension)
                .distinct()
                .forEach(dimension ->
                        areaStorage.saveAreas(getAreasInDimension(dimension, true), dimension)
                );
    }


    public void addAreaUpdatedListener(BiConsumer<Area, AreaOperation> listener) {
        areaUpdatedListeners.add(listener);
    }

    public void removeAreaUpdatedListener(BiConsumer<Area, AreaOperation> listener) {
        areaUpdatedListeners.remove(listener);
    }
}
