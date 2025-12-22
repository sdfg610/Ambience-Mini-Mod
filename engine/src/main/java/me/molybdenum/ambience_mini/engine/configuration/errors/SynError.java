package me.molybdenum.ambience_mini.engine.configuration.errors;

public record SynError(int line, int column, String message) implements LoadError { }
