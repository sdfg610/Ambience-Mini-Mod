package me.molybdenum.ambience_mini.engine.client.configuration.messages;

public sealed interface Message permits ExcError, SemError, SemWarning, SynError {
    boolean isError();
}
