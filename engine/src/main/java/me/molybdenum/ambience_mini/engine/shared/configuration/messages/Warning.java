package me.molybdenum.ambience_mini.engine.shared.configuration.messages;

public record Warning(int line, String message) implements Message
{
    @Override
    public boolean isError() {
        return false;
    }
}
