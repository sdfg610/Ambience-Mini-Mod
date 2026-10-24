package me.molybdenum.ambience_mini.deps.jid3lib.accessors;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomFileAccessor implements RandomAccessor {
    private final RandomAccessFile file;


    public RandomFileAccessor(RandomAccessFile file) {
        this.file = file;
    }


    @Override
    public long getPosition() throws IOException {
        return file.getFilePointer();
    }

    @Override
    public void seek(long position) throws IOException {
        file.seek(position);
    }


    @Override
    public long length() throws IOException {
        return file.length();
    }

    @Override
    public void setLength(long newLength) throws IOException {
        file.setLength(newLength);
    }


    @Override
    public void skipBytes(int length) throws IOException {
        file.skipBytes(length);
    }


    @Override
    public int read(byte[] buffer) throws IOException {
        return file.read(buffer);
    }

    @Override
    public void read(byte[] buffer, int offset, int length) throws IOException {
        file.read(buffer, offset, length);
    }

    @Override
    public byte readByte() throws IOException {
        return file.readByte();
    }

    @Override
    public short readShort() throws IOException {
        return file.readShort();
    }

    @Override
    public int readInt() throws IOException {
        return file.readInt();
    }


    @Override
    public void write(byte[] buffer) throws IOException {
        file.write(buffer);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        file.write(buffer, offset, length);
    }

    @Override
    public void writeByte(int value) throws IOException {
        file.writeByte(value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        file.writeShort(value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        file.writeInt(value);
    }


    @Override
    public void close() throws IOException {
        file.close();
    }

    @Override
    public void syncFD() throws IOException {
        file.getFD().sync();
    }

    @Override
    public void closeChannel() throws IOException {
        file.getChannel().close();
    }
}
