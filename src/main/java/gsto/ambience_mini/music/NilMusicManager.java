package gsto.ambience_mini.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class NilMusicManager extends MusicManager {
    public NilMusicManager(Minecraft pMinecraft) {
        super(pMinecraft);
    }

    public void tick() {

    }

    public void startPlaying(@NotNull Music pSelector) {

    }

    public void stopPlaying() {

    }
}
