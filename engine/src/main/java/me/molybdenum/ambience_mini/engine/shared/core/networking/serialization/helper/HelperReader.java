package me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HelperReader implements AmReader {
    ByteBuffer buf;


    public HelperReader(byte[] arr) {
        this.buf = ByteBuffer.wrap(arr);
    }


    @Override
    public boolean readBoolean() {
        return buf.get() != 0;
    }

    @Override
    public int readInt() {
        return buf.getInt();
    }

    @Override
    public double readDouble() {
        return buf.getDouble();
    }

    @Override
    public String readString() {
        boolean hasValue = readBoolean();
        if (hasValue)
            return new String(readByteArray());
        return null;
    }

    @Override
    public byte[] readByteArray() {
        var arr = new byte[readInt()];
        buf.get(arr);
        return arr;
    }
}
