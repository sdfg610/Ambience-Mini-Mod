package me.molybdenum.ambience_mini.engine;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.music.Monitor;

import java.util.ArrayList;

public class BaseAmbienceMini
{
    @SuppressWarnings("rawtypes")
    protected static BaseClientCore baseCore;

    private static final ArrayList<Runnable> onClientCoreInitListeners = new ArrayList<>();


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isVanillaPlayerEnabled() {
        if (baseCore == null) // Until Ambience Mini is actually loaded, don't start anything.
            return true;

        Monitor m = baseCore.getMonitor();
        return m == null || !m.isRunning() || m.isVanillaPlayerSelected();
    }


    public static void registerOnClientCoreInitListener(Runnable callback) {
        onClientCoreInitListeners.add(callback);
    }

    protected static void fireClientCoreInit() {
        for (var callback : onClientCoreInitListeners)
            callback.run();
    }
}
