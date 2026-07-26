package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.playlist;

public record Concat(Playlist left, Playlist right) implements Playlist { }
