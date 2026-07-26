package me.molybdenum.ambience_mini.engine.shared.configuration.messages;

public record SynError(int line, int column, String message) implements Message
{
    @Override
    public boolean isError() {
        return true;
    }
}
