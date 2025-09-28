package me.molybdenum.ambience_mini.engine.state;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseVolumeMonitor {
    private final Supplier<Boolean> _ignoreMasterVolume;
    private final HashSet<Consumer<Float>> volumeChangedHandlers = new HashSet<>();


    protected BaseVolumeMonitor(Supplier<Boolean> ignoreMasterVolume) {
        _ignoreMasterVolume = ignoreMasterVolume;

        initialize();
    }


    protected abstract void initialize();

    protected abstract float getMusicVolume();

    protected abstract float getMasterVolume();

    public float getVolume() {
        return getMusicVolume() *  (_ignoreMasterVolume.get() ? 1f : getMasterVolume());
    }


    public void registerVolumeHandler(Consumer<Float> consumer) {
        volumeChangedHandlers.add(consumer);
    }

    public void unregisterVolumeHandler(Consumer<Float> consumer) {
        volumeChangedHandlers.remove(consumer);
    }

    protected void handleVolumeChanged() {
        volumeChangedHandlers.forEach(handler -> handler.accept(getVolume()));
    }
}
