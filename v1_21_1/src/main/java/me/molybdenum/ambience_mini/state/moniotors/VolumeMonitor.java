package me.molybdenum.ambience_mini.state.moniotors;

import me.molybdenum.ambience_mini.engine.state.monitors.BaseVolumeMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeMonitor extends BaseVolumeMonitor {
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
                ObfuscationReflectionHelper.getPrivateValue(OptionInstance.class, _masterVolumeInstance, "onValueUpdate");
        Consumer<Double> onMusicVolumeUpdated =
                ObfuscationReflectionHelper.getPrivateValue(OptionInstance.class, _musicVolumeInstance, "onValueUpdate");

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
                OptionInstance.class, _masterVolumeInstance, onMasterVolumeUpdatedWrapper, "onValueUpdate"
        );
        ObfuscationReflectionHelper.setPrivateValue(
                OptionInstance.class, _musicVolumeInstance, onMusicVolumeUpdatedWrapper, "onValueUpdate"
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
