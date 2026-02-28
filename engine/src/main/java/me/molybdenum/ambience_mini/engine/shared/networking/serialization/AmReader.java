package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

public interface AmReader {
    boolean readBoolean();
    int readInt();
    String readString();

    default <T extends AmSerializable> T readTo(T obj) {
        obj.readFrom(this);
        return obj;
    }
}
