package gsto.ambience_mini.music.player.rule;

import gsto.ambience_mini.music.player.Music;

import java.util.List;

public record PlayRule(List<Music> playlist) implements Rule {
    @Override
    public NextMusic getNext() {
        return new NextMusic(playlist, false);
    }
}
