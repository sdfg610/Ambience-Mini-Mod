package me.molybdenum.ambience_mini.engine.client.configuration.messages;

public record SynError(int line, int column, String message) implements Message
{
    @Override
    public boolean isError() {
        return true;
    }
}
