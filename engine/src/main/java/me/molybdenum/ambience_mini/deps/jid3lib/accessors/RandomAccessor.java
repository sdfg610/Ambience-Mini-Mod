package me.molybdenum.ambience_mini.deps.jid3lib.accessors;

import java.io.IOException;

public interface RandomAccessor
{
    long getPosition() throws IOException;
    void seek(long position) throws IOException;

    long length() throws IOException;
    void setLength(long length) throws IOException;

    void skipBytes(int length) throws IOException;

    int read(byte[] buffer) throws IOException;
    void read(byte[] buffer, int offset, int length) throws IOException;
    byte readByte() throws IOException;
    short readShort() throws IOException;
    int readInt() throws IOException;

    void write(byte[] buffer) throws IOException;
    void write(byte[] buffer, int offset, int length) throws IOException;
    void writeByte(int value) throws IOException;
    void writeShort(int indexPoints) throws IOException;
    void writeInt(int size) throws IOException;

    void syncFD() throws IOException;
    void closeChannel() throws IOException;
    void close() throws IOException;
}
