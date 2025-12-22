package me.molybdenum.ambience_mini.engine.configuration.errors;

public record ExcError(Exception exception) implements LoadError { }
