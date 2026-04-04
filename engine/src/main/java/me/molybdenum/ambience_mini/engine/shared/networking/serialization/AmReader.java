package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

import java.util.ArrayList;
import java.util.function.Function;


public interface AmReader {
    boolean readBoolean();
    int readInt();
    double readDouble();
    String readString();


    default <T extends AmSerializable> ArrayList<T> readList(Function<AmReader, T> newT) {
        int length = readInt();
        ArrayList<T> lst = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            lst.add(newT.apply(this));
        return lst;
    }

    default ArrayList<String> readStringList() {
        int length = readInt();
        ArrayList<String> lst = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            lst.add(readString());
        return lst;
    }
}
