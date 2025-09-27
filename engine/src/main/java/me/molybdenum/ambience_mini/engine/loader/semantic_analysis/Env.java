package me.molybdenum.ambience_mini.engine.loader.semantic_analysis;

import me.molybdenum.ambience_mini.engine.player.Music;

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
