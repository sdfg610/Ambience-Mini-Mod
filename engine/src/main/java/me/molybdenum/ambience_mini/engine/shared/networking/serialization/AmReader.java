package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

import java.util.function.Supplier;


public interface AmReader {
    boolean readBoolean();
    int readInt();
    String readString();

    default <T extends AmSerializable> T read(Supplier<T> newT) {
        T obj = newT.get();
        obj.readFrom(this);
        return obj;
    }
}
