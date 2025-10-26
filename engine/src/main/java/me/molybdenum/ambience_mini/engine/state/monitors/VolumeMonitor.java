package me.molybdenum.ambience_mini.engine.state.monitors;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeMonitor {
    private float _masterVolume;
    private float _musicVolume;

    private final Supplier<Boolean> _ignoreMasterVolume;
    private final HashSet<Consumer<Float>> volumeChangedHandlers = new HashSet<>();


    public VolumeMonitor(BaseConfig config, float master, float music) {
        _ignoreMasterVolume = config.ignoreMasterVolume;
        _masterVolume = master;
        _musicVolume = music;
    }


    public float getMasterVolume() {
        return _masterVolume;
    }

    public void setMasterVolume(float volume) {
        _masterVolume = volume;
        handleVolumeChanged();
    }


    public float getMusicVolume() {
        return _musicVolume;
    }

    public void setMusicVolume(float volume) {
        _musicVolume = volume;
        handleVolumeChanged();
    }


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
