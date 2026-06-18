package me.molybdenum.ambience_mini.v1_21_1.mixin;

import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Final;
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
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    private SoundInstance currentMusic;


    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        if (!BaseAmbienceMini.isVanillaPlayerEnabled()) {
            if (currentMusic != null)
                stopPlaying();
            net.neoforged.neoforge.client.ClientHooks.selectMusic(minecraft.getSituationalMusic(), this.currentMusic);
            ci.cancel();
        }
    }
}
