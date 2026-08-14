package me.molybdenum.ambience_mini.engine.shared.music.streams;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class LazyPreAllocBuffer
{
    protected final int bufferSize;
    protected final byte[] buffer;
    protected int bytesLoaded = 0;


    public LazyPreAllocBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract void loadBufferTo(int targetPosition) throws IOException;

    public abstract void close() throws IOException;


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public int getBufferSize() {
        return bufferSize;
    }

    public int getBytesLoaded() {
        return bytesLoaded;
    }

    public boolean isFullyLoaded() {
        return bytesLoaded == bufferSize;
    }


    public int read(int position) throws IOException {
        return ensureLoaded(position)
            ? buffer[position] & 0xFF
            : -1;
    }

    public int read(int sourcePosition, byte @NotNull [] target, int targetPosition, int length) throws IOException {
        if (length <= 0)
            return sourcePosition >= bufferSize ? -1 : 0;
        if (!ensureLoaded(sourcePosition + length - 1))
            return -1;

        System.arraycopy(
                buffer, sourcePosition,
                target, targetPosition,
                length
        );

        return length;
    }

    public byte[] read(int sourcePosition, int length) throws IOException {
        if (length <= 0)
            return sourcePosition >= bufferSize ? null : new byte[0];
        if (!ensureLoaded(sourcePosition + length - 1))
            return null;

        byte[] data = new byte[length];
        System.arraycopy(
                buffer, sourcePosition,
                data, 0,
                length
        );

        return data;
    }

    protected boolean ensureLoaded(int targetPosition) throws IOException {
        if (targetPosition >= bufferSize)
            return false;
        if (targetPosition < bytesLoaded)
            return true;

        loadBufferTo(targetPosition);

        return targetPosition < bytesLoaded;
    }
}
