package me.molybdenum.ambience_mini.engine.client.music.streams;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class LazyPreAllocatedBuffer
{
    private final InputStream stream;
    private final int bufferSize;
    private final int loadChunkSize;

    private final byte[] buffer;
    private int loadedBytes = 0;


    public LazyPreAllocatedBuffer(InputStream stream, int bufferSize, int loadChunkSize) {
        this.stream = stream;
        this.bufferSize = bufferSize;
        this.loadChunkSize = loadChunkSize;

        this.buffer = new byte[bufferSize];
    }


    public int read(int position) throws IOException {
        return ensureLoaded(position)
            ? buffer[position] & 0xFF
            : -1;
    }

    public int read(int sourcePosition, byte @NotNull [] target, int targetPosition, int length) throws IOException {
        if (length <= 0)
            return 0;
        if (!ensureLoaded(sourcePosition + length - 1))
            return -1;

        System.arraycopy(
                buffer, sourcePosition,
                target, targetPosition,
                length
        );

        return length;
    }

    private synchronized boolean ensureLoaded(int targetPosition) throws IOException {
        if (targetPosition >= bufferSize)
            return false;
        if (targetPosition < loadedBytes)
            return true;

        int bytesToLoad = Math.min(
                (((targetPosition - loadedBytes) / loadChunkSize) + 1) * loadChunkSize,
                bufferSize - loadedBytes
        );
        loadedBytes += Math.max(0, stream.read(buffer, loadedBytes, bytesToLoad));  // Max(0, ...) to prevent adding '-1'

        return targetPosition < loadedBytes;
    }


    public int size() {
        return bufferSize;
    }


    public void close() throws IOException {
        stream.close();
    }
}
