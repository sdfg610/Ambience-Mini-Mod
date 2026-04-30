package me.molybdenum.ambience_mini.engine.shared.core.networking.serialization;


import java.util.Collection;
import java.util.List;

public interface AmWriter {
    void writeBoolean(boolean value);
    void writeInt(int value);
    void writeDouble(double value);
    void writeString(String value);


    default <T extends AmSerializable> void writeList(List<T> list) {
        writeInt(list.size());
        list.forEach(elem -> elem.writeTo(this));
    }

    default void writeStringList(Collection<String> list) {
        writeInt(list.size());
        list.forEach(this::writeString);
    }

    default void writeStringArray(String[] arr) {
        writeInt(arr.length);
        for (var str : arr)
            writeString(str);
    }
}
