package me.molybdenum.ambience_mini.engine.client.core.state;

import java.util.HashSet;

public class VolumeState {
    private static float _masterVolume;
    private static float _musicVolume;
    private static float _recordVolume;

    private static final HashSet<Runnable> musicVolumeChangedListeners = new HashSet<>();


    public static void init(float master, float music, float record) {
        _masterVolume = master;
        _musicVolume = music;
        _recordVolume = record;
    }


    public static float getMasterVolume() {
        return _masterVolume;
    }

    public static void setMasterVolume(float volume) {
        _masterVolume = volume;
        fireVolumeChanged();
    }


    public static float getMusicVolume() {
        return _musicVolume;
    }

    public static void setMusicVolume(float volume) {
        _musicVolume = volume;
        fireVolumeChanged();
    }


    public static float getTrueRecordVolume() {
        return _masterVolume * _recordVolume;
    }

    public static void setRecordVolume(float volume) {
        _recordVolume = volume;
        fireVolumeChanged();
    }


    public static void registerVolumeListener(Runnable consumer) {
        musicVolumeChangedListeners.add(consumer);
    }

    public static void unregisterVolumeListener(Runnable consumer) {
        musicVolumeChangedListeners.remove(consumer);
    }

    private static void fireVolumeChanged() {
        musicVolumeChangedListeners.forEach(Runnable::run);
    }
}
