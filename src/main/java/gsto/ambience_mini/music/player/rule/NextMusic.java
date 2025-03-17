package gsto.ambience_mini.music.player.rule;

import gsto.ambience_mini.music.player.Music;
import java.util.List;

public record NextMusic(List<Music> playlist, boolean isInterrupt) {
    public NextMusic asInterrupt() {
        return new NextMusic(playlist, true);
    }
}
