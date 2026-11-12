package me.molybdenum.ambience_mini.engine;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.music.MusicEngine;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BaseAmbienceMini
{
    private static final ArrayList<Runnable> onClientCoreInitListeners = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    protected static BaseClientCore baseClientCore;


    @SuppressWarnings("rawtypes")
    public static BaseClientCore getBaseClientCore() {
        return baseClientCore;
    }


    // Initialization listeners
    public static void registerOnClientCoreInitListener(Runnable callback) {
        synchronized (onClientCoreInitListeners) {
            onClientCoreInitListeners.add(callback);
        }
    }

    public static <T> void loadIfInitializedOrRegisterListener(Supplier<T> coreSupplier, Consumer<T> callback) {
        synchronized (onClientCoreInitListeners) {
            var core = coreSupplier.get();
            if (core == null)
                onClientCoreInitListeners.add(() -> callback.accept(coreSupplier.get()));
            else
                callback.accept(core);
        }
    }

    protected static void fireClientCoreInit() {
        synchronized (onClientCoreInitListeners) {
            for (var callback : onClientCoreInitListeners)
                callback.run();
        }
    }


    // Vanilla player
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isVanillaPlayerEnabled() {
        if (baseClientCore == null) // Until Ambience Mini is actually loaded, don't disable vanilla player.
            return true;

        MusicEngine m = baseClientCore.getMusicEngine();
        return m == null || !m.isRunning() || m.isVanillaPlayerSelected();
    }
}
