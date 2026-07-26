package me.molybdenum.ambience_mini.engine.shared.configuration.messages;

public record SemError(int line, String message) implements Message
{
    @Override
    public boolean isError() {
        return true;
    }
}
