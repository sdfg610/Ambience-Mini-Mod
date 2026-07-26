package me.molybdenum.ambience_mini.engine.shared.configuration.messages;

public sealed interface Message permits ExcError, SemError, SemWarning, SynError {
    boolean isError();
}
