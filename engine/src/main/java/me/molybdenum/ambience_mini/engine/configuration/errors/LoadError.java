package me.molybdenum.ambience_mini.engine.configuration.errors;

public sealed interface LoadError permits ExcError, SemError, SynError {
}
