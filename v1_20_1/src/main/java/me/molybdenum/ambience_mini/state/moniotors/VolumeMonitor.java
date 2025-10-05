package me.molybdenum.ambience_mini.state.moniotors;

import me.molybdenum.ambience_mini.engine.state.monitors.BaseVolumeMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public class VolumeMonitor extends BaseVolumeMonitor {
    private static final String OBF_MC_OPTION_ON_VALUE_UPDATE = "f_231479_";

    OptionInstance<Double> _masterVolumeInstance = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER);
    OptionInstance<Double> _musicVolumeInstance = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MUSIC);


    public VolumeMonitor(Supplier<Boolean> ignoreMasterVolume) {
        super(ignoreMasterVolume);
    }


    @Override
    protected void initialize()
    {
        _masterVolumeInstance = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER);
        _musicVolumeInstance = Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MUSIC);

        Consumer<Double> onMasterVolumeUpdated =
                ObfuscationReflectionHelper.getPrivateValue(OptionInstance.class, _masterVolumeInstance, OBF_MC_OPTION_ON_VALUE_UPDATE);
        Consumer<Double> onMusicVolumeUpdated =
                ObfuscationReflectionHelper.getPrivateValue(OptionInstance.class, _musicVolumeInstance, OBF_MC_OPTION_ON_VALUE_UPDATE);

        if (onMasterVolumeUpdated == null)
            throw new RuntimeException("'onMasterVolumeUpdated' is null!");
        if (onMusicVolumeUpdated == null)
            throw new RuntimeException("'onMusicVolumeUpdated' is null!");

        Consumer<Double> onMasterVolumeUpdatedWrapper = volume -> {
            super.handleVolumeChanged();
            onMasterVolumeUpdated.accept(volume);
        };
        Consumer<Double> onMusicVolumeUpdatedWrapper = volume -> {
            super.handleVolumeChanged();
            onMusicVolumeUpdated.accept(volume);
        };

        ObfuscationReflectionHelper.setPrivateValue(
                OptionInstance.class, _masterVolumeInstance, onMasterVolumeUpdatedWrapper, OBF_MC_OPTION_ON_VALUE_UPDATE
        );
        ObfuscationReflectionHelper.setPrivateValue(
                OptionInstance.class, _musicVolumeInstance, onMusicVolumeUpdatedWrapper, OBF_MC_OPTION_ON_VALUE_UPDATE
        );
    }

    @Override
    protected float getMusicVolume() {
        return _musicVolumeInstance.get().floatValue();
    }

    @Override
    protected float getMasterVolume() {
        return _masterVolumeInstance.get().floatValue();
    }
}
