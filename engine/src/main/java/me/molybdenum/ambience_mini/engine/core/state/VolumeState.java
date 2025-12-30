package me.molybdenum.ambience_mini.engine.core.state;

import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeState {
    private static float _masterVolume;
    private static float _musicVolume;
    private static float _recordVolume;

    private static Supplier<Boolean> _ignoreMasterVolume = () -> true;
    private static final HashSet<Consumer<Float>> musicVolumeChangedHandlers = new HashSet<>();


    public static void init(BaseClientConfig config, float master, float music, float record) {
        _ignoreMasterVolume = config.ignoreMasterVolume;
        _masterVolume = master;
        _musicVolume = music;
        _recordVolume = record;
    }


    public static float getMasterVolume() {
        return _masterVolume;
    }

    public static void setMasterVolume(float volume) {
        _masterVolume = volume;
        handleMusicVolumeChanged();
    }


    public static float getMusicVolume() {
        return _musicVolume;
    }

    public static void setMusicVolume(float volume) {
        _musicVolume = volume;
        handleMusicVolumeChanged();
    }


    public static float getRecordVolume() {
        return _recordVolume;
    }

    public static void setRecordVolume(float volume) {
        _recordVolume = volume;
    }


    public static float getTrueMusicVolume() {
        return _musicVolume * (_ignoreMasterVolume.get() ? 1f : getMasterVolume());
    }

    public static float getTrueRecordVolume() {
        return _recordVolume * getMasterVolume();
    }


    public static void registerVolumeHandler(Consumer<Float> consumer) {
        musicVolumeChangedHandlers.add(consumer);
    }

    public static void unregisterVolumeHandler(Consumer<Float> consumer) {
        musicVolumeChangedHandlers.remove(consumer);
    }

    private static void handleMusicVolumeChanged() {
        musicVolumeChangedHandlers.forEach(handler -> handler.accept(getTrueMusicVolume()));
    }
}
