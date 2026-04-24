package me.molybdenum.ambience_mini.v1_18_2.mixin;

import me.molybdenum.ambience_mini.v1_18_2.AmbienceMini;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Shadow
    private boolean loaded;

    @Inject(at = @At("TAIL"), method = "reload")
    public void reload(CallbackInfo ci) {
        if (loaded)
            AmbienceMini.onSoundEngineLoaded();
    }
}
