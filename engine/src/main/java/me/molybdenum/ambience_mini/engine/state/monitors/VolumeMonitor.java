package me.molybdenum.ambience_mini.engine.state.monitors;

import me.molybdenum.ambience_mini.engine.setup.BaseClientConfig;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeMonitor {
    private static float _masterVolume;
    private static float _musicVolume;

    private static Supplier<Boolean> _ignoreMasterVolume = () -> true;
    private static final HashSet<Consumer<Float>> volumeChangedHandlers = new HashSet<>();


    public static void init(BaseClientConfig config, float master, float music) {
        _ignoreMasterVolume = config.ignoreMasterVolume;
        _masterVolume = master;
        _musicVolume = music;
    }


    public static float getMasterVolume() {
        return _masterVolume;
    }

    public static void setMasterVolume(float volume) {
        _masterVolume = volume;
        handleVolumeChanged();
    }


    public static float getMusicVolume() {
        return _musicVolume;
    }

    public static void setMusicVolume(float volume) {
        _musicVolume = volume;
        handleVolumeChanged();
    }


    public static float getVolume() {
        return getMusicVolume() *  (_ignoreMasterVolume.get() ? 1f : getMasterVolume());
    }


    public static void registerVolumeHandler(Consumer<Float> consumer) {
        volumeChangedHandlers.add(consumer);
    }

    public static void unregisterVolumeHandler(Consumer<Float> consumer) {
        volumeChangedHandlers.remove(consumer);
    }

    private static void handleVolumeChanged() {
        volumeChangedHandlers.forEach(handler -> handler.accept(getVolume()));
    }
}
