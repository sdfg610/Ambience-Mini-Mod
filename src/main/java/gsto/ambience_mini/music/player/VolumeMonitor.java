package gsto.ambience_mini.music.player;

import gsto.ambience_mini.setup.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.HashSet;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class VolumeMonitor {
    private static final String OBF_MC_OPTION_ON_VALUE_UPDATE = "f_231479_";
    private static boolean listenersCreated = false;

    private static float masterVolume;
    private static float musicVolume;

    private static final HashSet<Consumer<Float>> volumeChangedHandlers = new HashSet<>();


    public static void init() {
        injectVolumeListeners();

        masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        musicVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC);
    }

    private static void injectVolumeListeners() {
        if (listenersCreated)
            return;

        OptionInstance<Double> masterVolume = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER);
        OptionInstance<Double> musicVolume = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MUSIC);

        Consumer<Double> onMasterVolumeUpdated =
                ObfuscationReflectionHelper.getPrivateValue(OptionInstance.class, masterVolume, OBF_MC_OPTION_ON_VALUE_UPDATE);
        Consumer<Double> onMusicVolumeUpdated =
                ObfuscationReflectionHelper.getPrivateValue(OptionInstance.class, musicVolume, OBF_MC_OPTION_ON_VALUE_UPDATE);

        if (onMasterVolumeUpdated == null)
            throw new RuntimeException("'onMasterVolumeUpdated' is null!");
        if (onMusicVolumeUpdated == null)
            throw new RuntimeException("'onMusicVolumeUpdated' is null!");

        Consumer<Double> onMasterVolumeUpdatedWrapper = volume -> {
            VolumeMonitor.setMasterVolume(volume.floatValue());
            onMasterVolumeUpdated.accept(volume);
        };
        Consumer<Double> onMusicVolumeUpdatedWrapper = volume -> {
            VolumeMonitor.setMusicVolume(volume.floatValue());
            onMusicVolumeUpdated.accept(volume);
        };

        ObfuscationReflectionHelper.setPrivateValue(
                OptionInstance.class, masterVolume, onMasterVolumeUpdatedWrapper, OBF_MC_OPTION_ON_VALUE_UPDATE
        );
        ObfuscationReflectionHelper.setPrivateValue(
                OptionInstance.class, musicVolume, onMusicVolumeUpdatedWrapper, OBF_MC_OPTION_ON_VALUE_UPDATE
        );

        listenersCreated = true;
    }


    public static float getVolume() {
        return getMusicVolume() * getMasterVolume();
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    public static float getMasterVolume() {
        return Config.ignoreMasterVolume.get() ? 1f : masterVolume;
    }


    public static void setMusicVolume(float musicVolume) {
        VolumeMonitor.musicVolume = musicVolume;
        volumeChangedHandlers.forEach(handler -> handler.accept(getVolume()));
    }

    public static void setMasterVolume(float masterVolume) {
        VolumeMonitor.masterVolume = masterVolume;
        volumeChangedHandlers.forEach(handler -> handler.accept(getVolume()));
    }


    public static boolean registerVolumeHandler(Consumer<Float> consumer) {
        return volumeChangedHandlers.add(consumer);
    }

    public static boolean unregisterVolumeHandler(Consumer<Float> consumer) {
        return volumeChangedHandlers.remove(consumer);
    }
}
