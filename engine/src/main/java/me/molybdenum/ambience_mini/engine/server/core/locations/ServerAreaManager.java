package me.molybdenum.ambience_mini.engine.server.core.locations;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.areas.AreaOperation;
import me.molybdenum.ambience_mini.engine.shared.areas.AreaStorage;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ServerAreaManager
{
    private final ArrayList<BiConsumer<Area, AreaOperation>> updateListeners = new ArrayList<>();

    private boolean isLoaded = false;
    private final Map<Integer, Area> areas = new ConcurrentHashMap<>();
    private final Set<String> dimensions = new HashSet<>();

    private Logger logger;

    private AreaStorage areaStorage;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.logger != null)
            throw new RuntimeException("Multiple calls to 'BaseServerAreaManager.init'!");

        this.logger = core.logger;
        this.areaStorage = new AreaStorage(logger, core.getAmStoragePath());
    }


    public Area getAreaById(int id) {
        return areas.get(id);
    }

    public List<Area> getAreasVisibleTo(String playerUUID) {
        return areas.values().stream().filter(
                area -> area.canBeSeenBy(playerUUID)
        ).toList();
    }


    public Optional<String> createArea(Area area) {
        area.id = findFreeId();
        return putArea(area);
    }

    private int findFreeId() {
        synchronized (areas) {
            return areas.values().stream().map(area -> area.id).reduce(Integer::max).orElse(0) + 1;
        }
    }


    public Optional<String> putArea(Area area) {
        var error = area.validate();
        if (error.isPresent())
            return error;

        synchronized (areas) {
            areas.put(area.id, area);
            dimensions.add(area.dimension);
        }

        updateListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
        return Optional.empty();
    }

    public void deleteArea(int id) {
        Area oldArea;
        synchronized (areas) {
            oldArea = areas.remove(id);
        }
        if (oldArea != null)
            updateListeners.forEach(listener -> listener.accept(oldArea, AreaOperation.DELETE));
    }


    public void loadAllAreas() {
        if (isLoaded)
            throw new RuntimeException("Cannot load areas twice!");

        var result = areaStorage.loadAllAreas();
        if (result.isPresent()) {
            result.get().forEach(area -> areas.put(area.id, area));
            isLoaded = true;
        }
        else
            logger.error("Could not load areas! See logs for more.");
    }

    public void saveAllAreas() {
        if (isLoaded) {
            dimensions.forEach(this::saveAreasForDimensionIfLoaded);
        }
    }

    public void saveAreasForDimensionIfLoaded(String dimensionID) {
        if (isLoaded) {
            List<Area> areasInDimension;
            synchronized (areas) {
                areasInDimension = areas.values().stream()
                        .filter(area -> area.dimension.equals(dimensionID))
                        .toList();
            }
            areaStorage.saveAreas(areasInDimension, dimensionID);
        }
    }


    public void addUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.remove(listener);
    }
}
