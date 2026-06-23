package me.molybdenum.ambience_mini.v1_20_1.mixin;

import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    @Shadow
    public abstract void stopPlaying();

    @Shadow
    @Nullable
    private SoundInstance currentMusic;


    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        if (currentMusic != null && !BaseAmbienceMini.isVanillaPlayerEnabled()) {
            stopPlaying();
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "startPlaying", cancellable = true)
    public void startPlaying(Music p_120185_, CallbackInfo ci) {
        if (!BaseAmbienceMini.isVanillaPlayerEnabled())
            ci.cancel();
    }
}
