package me.molybdenum.ambience_mini.engine.client.configuration.messages;

public record SemWarning(int line, String message) implements Message
{
    @Override
    public boolean isError() {
        return false;
    }
}
