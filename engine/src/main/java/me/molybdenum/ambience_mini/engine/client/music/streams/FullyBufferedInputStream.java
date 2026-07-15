package me.molybdenum.ambience_mini.engine.client.music.streams;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class FullyBufferedInputStream extends InputStream {
    private final LazyPreAllocatedBuffer buffer;
    private final boolean autoCloseBuffer;
    private final int bufferSize;

    private int position = 0;
    private int markedPos = 0;


    public FullyBufferedInputStream(LazyPreAllocatedBuffer buffer, boolean autoCloseBuffer) {
        this.buffer = buffer;
        this.autoCloseBuffer = autoCloseBuffer;

        this.bufferSize = buffer.size();
    }


    @Override
    public int read() throws IOException {
        return buffer.read(position++);
    }

    @Override
    public int read(byte @NotNull [] buf) throws IOException{
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte @NotNull [] buf, int s, int len) throws IOException{
        var boundedLength = Math.min(len, bufferSize - position);
        int length = buffer.read(position, buf, s, boundedLength);
        position += Math.max(0, length); // Max(0, ...) to prevent adding '-1'
        return length;
    }

    @Override
    public long skip(long n) throws IOException{
        int oldPos = position;
        seek(Math.min(position + n, bufferSize));
        return position - oldPos;
    }

    @Override
    public int available() throws IOException{
        return bufferSize - position;
    }

    @Override
    public void close() throws IOException {
        if (autoCloseBuffer)
            buffer.close();
    }


    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int ignored) {
        markedPos = position;
    }

    @Override
    public synchronized void reset() {
        position = markedPos;
    }


    public long getLength() {
        return bufferSize;
    }

    public long tell() {
        return position;
    }

    public void seek(long pos) throws IOException{
        if (pos < 0)
            throw new IOException("Negative seek offset");
        else if (pos > bufferSize)
            throw new IOException("Cannot seek to position '" + pos + "' with buffer size '" + bufferSize + "'");

        position = Math.toIntExact(pos);
    }
}
