package me.molybdenum.ambience_mini.state.monitors;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.state.monitors.BaseVolumeMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

import java.util.function.Supplier;

public class VolumeMonitor extends BaseVolumeMonitor {
    private float _masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
    private float _musicVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC);


    public VolumeMonitor(Supplier<Boolean> ignoreMasterVolume) {
        super(ignoreMasterVolume);
    }


    @Override
    protected void initialize()
    {
        // AmbienceMini.onVolumeChanged is invoked by SoundManagerMixin
        AmbienceMini.onVolumeChanged = (pair) -> {
            switch (pair.getA()) {
                case MASTER -> _masterVolume = pair.getB();
                case MUSIC -> _musicVolume = pair.getB();
                default -> {
                    return;
                }
            }
            super.handleVolumeChanged();
        };
    }

    @Override
    protected float getMusicVolume() {
        return _musicVolume;
    }

    @Override
    protected float getMasterVolume() {
        return _masterVolume;
    }
}
