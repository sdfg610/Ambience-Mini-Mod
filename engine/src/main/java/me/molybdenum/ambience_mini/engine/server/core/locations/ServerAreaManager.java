package me.molybdenum.ambience_mini.engine.server.core.locations;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.areas.AreaOperation;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ServerAreaManager
{
    private final ArrayList<BiConsumer<Area, AreaOperation>> updateListeners = new ArrayList<>();
    private final ConcurrentHashMap<Integer, Area> areas = new ConcurrentHashMap<>();

    private Supplier<Path> getWorldRootPath;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.getWorldRootPath != null)
            throw new RuntimeException("Multiple calls to 'BaseServerAreaManager.init'!");
        this.getWorldRootPath = core::getWorldRootPath;
    }


    public Path getAreaStoragePath() {
        return getWorldRootPath.get().resolve(Common.AM_STORAGE_DIRECTORY);
    }


    public Area getAreaById(int id) {
        return areas.get(id);
    }

    public List<Area> getAreasInDimension(String dimension) {
        return areas.values().stream()
                .filter(area -> area.dimension.equals(dimension))
                .toList();
    }

    public List<Area> getAreasVisibleTo(String playerUUID) {
        return areas.values().stream().filter(
                area -> area.canBeSeenBy(playerUUID)
        ).toList();
    }


    public void createArea(Area area) {
        area.id = findFreeId();
        putArea(area);
    }

    private int findFreeId() {
        return areas.values().stream().map(area -> area.id).reduce(Integer::max).orElse(0) + 1;
    }


    public void putArea(Area area) {
        areas.put(area.id, area);
        updateListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
    }

    public void deleteArea(int id) {
        Area oldArea = areas.remove(id);
        if (oldArea != null)
            updateListeners.forEach(listener -> listener.accept(oldArea, AreaOperation.DELETE));
    }


    public void loadAreas() {
        File areasDir = getAreaStoragePath().toFile();
        if (!areasDir.isDirectory())
            return;

        var fileEntries = areasDir.listFiles();
        if (fileEntries == null)
            return;

        Arrays.stream(fileEntries)
                .filter(file -> file.isFile() && file.getName().matches(".*\\+areas\\.json"))
                .forEach(file -> {
                    // TODO: !!!
                });
    }

    private void readFrom(File file) {
        // TODO: !!!
    }


    public void saveAreas(String dimensionID) {
        // TODO: !!!
    }


    public void addUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.remove(listener);
    }
}
