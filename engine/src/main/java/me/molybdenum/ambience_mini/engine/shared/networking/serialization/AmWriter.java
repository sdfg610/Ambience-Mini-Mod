package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

public interface AmWriter {
    void writeBoolean(boolean value);
    void writeInt(int value);
    void writeString(String value);
}
