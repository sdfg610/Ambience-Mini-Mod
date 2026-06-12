package me.molybdenum.ambience_mini.engine.client.configuration.messages;

public record ExcError(Exception exception) implements Message
{
    @Override
    public boolean isError() {
        return true;
    }
}
