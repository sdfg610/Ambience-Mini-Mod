package me.molybdenum.ambience_mini.engine.client.configuration.errors;

public record SemError(int line, String message) implements LoadError { }
