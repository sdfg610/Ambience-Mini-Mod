package gsto.ambience_mini.music.assorted;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.NotNull;

public class NilMusicManager extends MusicManager {
    public NilMusicManager(Minecraft pMinecraft) {
        super(pMinecraft);
    }

    public void tick() { }

    public void startPlaying(@NotNull Music pSelector) { }

    public void stopPlaying() { }
}
