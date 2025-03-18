package gsto.ambience_mini.music.player.rule;

import gsto.ambience_mini.music.player.Music;
import java.util.List;

public record PlaylistChoice(List<Music> playlist, boolean isInterrupt, boolean isInstant) {
    public PlaylistChoice asInterrupt() {
        return new PlaylistChoice(playlist, true, isInstant);
    }
}
