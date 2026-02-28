package me.molybdenum.ambience_mini.engine.client.configuration.errors;

public record ExcError(Exception exception) implements LoadError { }
