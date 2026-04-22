package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeState {
    private static float _masterVolume;
    private static float _musicVolume;
    private static float _recordVolume;

    private static final HashSet<Consumer<Float>> musicVolumeChangedListeners = new HashSet<>();


    public static void init(BaseClientConfig config, float master, float music, float record) {
        _masterVolume = master;
        _musicVolume = music;
        _recordVolume = record;
    }


    public static float getMasterVolume() {
        return _masterVolume;
    }

    public static void setMasterVolume(float volume) {
        _masterVolume = volume;
    }


    public static float getMusicVolume() {
        return _musicVolume;
    }

    public static void setMusicVolume(float volume) {
        _musicVolume = volume;
        fireMusicVolumeChanged();
    }


    public static float getTrueRecordVolume() {
        return _masterVolume * _recordVolume;
    }

    public static void setRecordVolume(float volume) {
        _recordVolume = volume;
    }


    public static void registerMusicVolumeListener(Consumer<Float> consumer) {
        musicVolumeChangedListeners.add(consumer);
    }

    public static void unregisterVolumeListener(Consumer<Float> consumer) {
        musicVolumeChangedListeners.remove(consumer);
    }

    private static void fireMusicVolumeChanged() {
        musicVolumeChangedListeners.forEach(handler -> handler.accept(_musicVolume));
    }
}
