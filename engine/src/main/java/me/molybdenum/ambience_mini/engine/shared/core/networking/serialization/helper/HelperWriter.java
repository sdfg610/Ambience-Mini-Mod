package me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HelperWriter implements AmWriter {
    private final ByteBuffer convert = ByteBuffer.allocate(8);
    private final ByteArrayOutputStream stream;


    public HelperWriter(ByteArrayOutputStream stream) {
        this.stream = stream;
    }

    public HelperWriter(int size) {
        this(new ByteArrayOutputStream(size));
    }

    public HelperWriter() {
        this(1024);
    }


    public byte[] getBytes() {
        return stream.toByteArray();
    }


    @Override
    public void writeBoolean(boolean value) {
        stream.write(value ? 1 : 0);
    }

    @Override
    public void writeInt(int value) {
        convert.rewind();
        stream.write(convert.putInt(value).array(), 0, Integer.BYTES);
    }

    @Override
    public void writeDouble(double value) {
        convert.rewind();
        stream.write(convert.putDouble(value).array(), 0, Double.BYTES);
    }

    @Override
    public void writeString(String value) {
        if (value == null)
            writeBoolean(false);
        else {
            writeBoolean(true);
            writeByteArray(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void writeByteArray(byte[] value) {
        writeInt(value.length);
        stream.writeBytes(value);
    }
}
