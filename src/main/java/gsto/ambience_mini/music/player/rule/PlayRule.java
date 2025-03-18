package gsto.ambience_mini.music.player.rule;

import gsto.ambience_mini.music.player.Music;

import java.util.List;

public record PlayRule(List<Music> playlist, boolean isInstant) implements Rule {
    @Override
    public PlaylistChoice getNext() {
        return new PlaylistChoice(playlist, false, isInstant);
    }
}
