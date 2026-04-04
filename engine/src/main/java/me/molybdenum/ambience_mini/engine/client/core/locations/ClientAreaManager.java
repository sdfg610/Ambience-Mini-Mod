package me.molybdenum.ambience_mini.engine.client.core.locations;


import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.areas.AreaOperation;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.GetAreasMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ClientAreaManager
{
    private BaseClientNetworkManager networkManager;

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

    public List<Area> getAreasInDimension(String dimension) {
        return areas.values().stream()
                .filter(area -> area.dimension.equals(dimension))
                .toList();
    }

    public List<Area> getIntersectingAreas(String dimension, Vector3d position) {
        return areas.values().stream()
                .filter(area -> area.dimension.equals(dimension) && area.contains(position))
                .toList();
    }


    public void putArea(Area area) {
        Area oldArea = areas.put(area.id, area);

        if (oldArea != null && oldArea.owner.isLocal())
            saveLocalAreas();

        areaUpdatedListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
    }

    public void deleteArea(int id) {
        Area oldArea = areas.remove(id);

        if (oldArea != null && oldArea.owner.isLocal())
            saveLocalAreas();

        if (oldArea != null)
            areaUpdatedListeners.forEach(listener -> listener.accept(oldArea, AreaOperation.DELETE));
    }


    public void loadAreas() {
        areas.clear();
        networkManager.sendToServer(new GetAreasMessage());
        // TODO: Load from local
    }

    public void saveLocalAreas() {
        // TODO
    }


    public void addAreaUpdatedListener(BiConsumer<Area, AreaOperation> listener) {
        areaUpdatedListeners.add(listener);
    }

    public void removeAreaUpdatedListener(BiConsumer<Area, AreaOperation> listener) {
        areaUpdatedListeners.remove(listener);
    }
}
