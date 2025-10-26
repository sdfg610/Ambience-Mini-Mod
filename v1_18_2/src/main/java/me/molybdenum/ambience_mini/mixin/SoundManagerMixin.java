package me.molybdenum.ambience_mini.mixin;

import me.molybdenum.ambience_mini.AmbienceMini;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.util.tuples.Pair;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(at = @At("HEAD"), method = "updateSourceVolume(Lnet/minecraft/sounds/SoundSource;F)V")
    public void onUpdateSourceVolume(SoundSource p_120359_, float p_120360_, CallbackInfo ignored)
    {
        if (AmbienceMini.volume != null) {
            switch (p_120359_) {
                case MASTER -> AmbienceMini.volume.setMasterVolume(p_120360_);
                case MUSIC -> AmbienceMini.volume.setMusicVolume(p_120360_);
            }
        }
    }
}
