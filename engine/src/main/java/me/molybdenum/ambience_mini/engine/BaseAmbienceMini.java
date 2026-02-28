package me.molybdenum.ambience_mini.engine;

import java.util.ArrayList;

public class BaseAmbienceMini
{
    private static final ArrayList<Runnable> onClientCoreInitListeners = new ArrayList<>();

    public static void registerOnClientCoreInitListener(Runnable callback) {
        onClientCoreInitListeners.add(callback);
    }

    protected static void fireClientCoreInit() {
        for (var callback : onClientCoreInitListeners)
            callback.run();
    }
}
