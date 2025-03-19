package gsto.ambience_mini.music.loader.semantic_analysis;

import gsto.ambience_mini.music.player.Music;

import java.util.ArrayList;

public class Env {
    public int inInterrupt = 0;
    public ArrayList<String> playlists = new ArrayList<>();

    public Env addPlaylist(String name) {
        playlists.add(name);
        return this;
    }

    public boolean hasPlaylist(String name) {
        return playlists.stream().anyMatch(pl -> pl.equals(name));
    }
}
