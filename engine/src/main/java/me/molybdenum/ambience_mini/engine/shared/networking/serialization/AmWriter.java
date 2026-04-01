package me.molybdenum.ambience_mini.engine.shared.networking.serialization;


public interface AmWriter {
    void writeBoolean(boolean value);
    void writeInt(int value);
    void writeString(String value);

    default <T extends AmSerializable> void writeNullable(T value) {
        if (value == null)
            writeBoolean(false);
        else {
            writeBoolean(true);
            value.writeTo(this);
        }
    }
}
