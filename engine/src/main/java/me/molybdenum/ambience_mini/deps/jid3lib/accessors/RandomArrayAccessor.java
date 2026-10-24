package me.molybdenum.ambience_mini.deps.jid3lib.accessors;

import java.io.EOFException;
import java.io.IOException;

public class RandomArrayAccessor implements RandomAccessor
{
    private final byte[] data;

    private int position = 0;


    public RandomArrayAccessor(byte[] data) {
        this.data = data;
    }


    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void seek(long newPosition) {
        this.position = Math.toIntExact(newPosition);
    }

    @Override
    public long length() {
        return data.length;
    }

    @Override
    public void setLength(long length) {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public void skipBytes(int length) {
        if (length > 0)
            position += length;
    }


    @Override
    public int read(byte[] buffer) {
        if (position >= data.length)
            return -1;

        int length = Math.min(data.length - position, buffer.length);
        System.arraycopy(data, position, buffer, 0, length);
        position += length;

        return length;
    }

    @Override
    public void read(byte[] buffer, int offset, int length) throws IOException {
        if (position >= data.length)
            return;

        int len = Math.min(data.length - position, Math.min(buffer.length-offset, length));
        System.arraycopy(data, position, buffer, offset, len);
        position += len;
    }

    @Override
    public byte readByte() throws IOException {
        if (position >= data.length)
            throw new EOFException();
        return data[position++];
    }

    @Override
    public short readShort() throws IOException {
        if (position+1 >= data.length)
            throw new EOFException();

        byte b1 = data[position++];
        byte b2 = data[position++];
        return (short)((b1 << 8) | b2);
    }

    @Override
    public int readInt() throws IOException {
        if (position+3 >= data.length)
            throw new EOFException();

        byte b1 = data[position++];
        byte b2 = data[position++];
        byte b3 = data[position++];
        byte b4 = data[position++];
        return (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
    }


    @Override
    public void write(byte[] buffer) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void write(byte[] buffer, int offset, int length) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void writeByte(int value) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void writeShort(int indexPoints) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void writeInt(int size) {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public void syncFD() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void closeChannel() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void close() { }
}
