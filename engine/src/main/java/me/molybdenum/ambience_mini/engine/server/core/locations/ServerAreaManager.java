package me.molybdenum.ambience_mini.engine.server.core.locations;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.areas.AreaOperation;
import me.molybdenum.ambience_mini.engine.shared.core.areas.AreaStorage;
import me.molybdenum.ambience_mini.engine.shared.utils.results.TextResult;
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
    private final Set<String> dirtyDimensions = new HashSet<>();

    private Logger logger;

    private AreaStorage areaStorage;
    private boolean areasDisabled;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.logger != null)
            throw new RuntimeException("Multiple calls to '" + getClass().getName() + ".init'!");

        this.logger = core.logger;
        this.areaStorage = new AreaStorage(logger, core.getAmStoragePath());

        this.areasDisabled = !core.serverConfig.enableAreas.get();
    }


    public boolean areasDisabled() {
        return areasDisabled;
    }


    public TextResult<Area> getAreaById(int id) {
        if (areasDisabled)
            return TextResult.fail(AmLang.MSG_AREAS_DISABLED.text());

        var area = areas.get(id);
        return area == null
                ? TextResult.fail(AmLang.MSG_NO_SUCH_AREA_ID.text(Integer.toString(id)))
                : TextResult.of(area);
    }

    public TextResult<List<Area>> getAreasVisibleTo(String playerUUID) {
        if (areasDisabled)
            return TextResult.fail(AmLang.MSG_AREAS_DISABLED.text());

        return TextResult.of(
                areas.values().stream().filter(
                        area -> area.canBeSeenBy(playerUUID)
                ).toList()
        );
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
            dirtyDimensions.add(area.dimension);
            updateListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
        }

        return Optional.empty();
    }

    public void deleteArea(int id) {
        Area oldArea;
        synchronized (areas) {
            oldArea = areas.remove(id);
            if (oldArea != null) {
                updateListeners.forEach(listener -> listener.accept(oldArea, AreaOperation.DELETE));
                dirtyDimensions.add(oldArea.dimension);
            }
        }
    }


    public void loadAllAreas() {
        if (isLoaded)
            throw new RuntimeException("Cannot load areas twice!");

        var result = areaStorage.loadAllAreas();
        if (result.isPresent()) {
            result.get().forEach(area -> areas.put(area.id, area));
            isLoaded = true;
            dirtyDimensions.clear();
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
        if (isLoaded && dirtyDimensions.contains(dimensionID)) {
            List<Area> areasInDimension;
            synchronized (areas) {
                areasInDimension = areas.values().stream()
                        .filter(area -> area.dimension.equals(dimensionID))
                        .toList();
                areaStorage.saveAreas(areasInDimension, dimensionID);
                dirtyDimensions.remove(dimensionID);
            }
        }
    }


    public void addUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.remove(listener);
    }
}
