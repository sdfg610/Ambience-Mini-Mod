package me.molybdenum.ambience_mini.server.data;

import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class AreaSavedData extends SavedData {
    ArrayList<Area> areas = new ArrayList<>();


    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public AreaSavedData load(CompoundTag tag) {
        AreaSavedData data = new AreaSavedData();
        // Load saved data
        return data;
    }
}
