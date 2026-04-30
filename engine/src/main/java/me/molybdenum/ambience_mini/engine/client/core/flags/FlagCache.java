package me.molybdenum.ambience_mini.engine.client.core.flags;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.StringVal;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers.ValueMap;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.GetFlagsMessage;

import java.util.HashMap;

public class FlagCache
{
    private BaseClientNetworkManager network;

    private final HashMap<String, String> flags = new HashMap<>();
    private ValueMap map = new ValueMap();


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (this.network != null)
            throw new RuntimeException("Multiple calls to 'ClientNameCache.init'!");

        network = core.networkManager;
    }


    public ValueMap getFlags() {
        return map;
    }

    public void putFlag(String id, String value) {
        synchronized (flags) {
            flags.put(id, value);
            updateMap();
        }
    }

    public void deleteFlag(String id) {
        synchronized (flags) {
            flags.remove(id);
            updateMap();
        }
    }

    private void updateMap() {
        synchronized (flags) {
            ValueMap newMap = new ValueMap();
            flags.forEach((id, value) -> newMap.put(new StringVal(id), new StringVal(value)));
            map = newMap;
        }
    }


    public void clear() {
        flags.clear();
    }

    public void loadFlags() {
        clear();
        network.sendToServer(new GetFlagsMessage());
    }
}
