package me.molybdenum.ambience_mini.engine.shared.core.networking.serialization;


import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AmWriter {
    void writeBoolean(boolean value);
    void writeInt(int value);
    void writeDouble(double value);
    void writeString(String value);
    void writeByteArray(byte[] value);


    default void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
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


    default <T extends AmSerializable> void writeList(List<T> list) {
        writeInt(list.size());
        list.forEach(elem -> elem.writeTo(this));
    }

    default <T extends AmSerializable> void writeStringKeyedMap(Map<String, T> map) {
        writeInt(map.size());
        for (var entry : map.entrySet()) {
            writeString(entry.getKey());
            var val = entry.getValue();
            if (val != null)
                val.writeTo(this);
        }
    }

    default <T extends AmSerializable> void write(T elem) {
        elem.writeTo(this);
    }
}
