package me.molybdenum.ambience_mini.engine.client.core.areas;


import me.molybdenum.ambience_mini.engine.shared.areas.Area;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiConsumer;

public abstract class BaseClientAreaManager
{
    private final ArrayList<BiConsumer<Area, AreaOperation>> updateListeners = new ArrayList<>();
    private final HashMap<Integer, Area> areas = new HashMap<>();

    public Collection<Area> areas() {
        return areas.values();
    }

    public void putArea(Area area) {
        areas.put(area.id, area);
        updateListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
    }

    public void deleteArea(int id) {
        Area area = areas.remove(id);
        if (area != null)
            updateListeners.forEach(listener -> listener.accept(area, AreaOperation.PUT));
    }



    public void addUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(BiConsumer<Area, AreaOperation> listener) {
        updateListeners.remove(listener);
    }



    public abstract String getStoragePath();
    // TODO: Loading and storing areas (server-based + client-based)

    public enum AreaOperation {
        PUT, DELETE
    }
}
