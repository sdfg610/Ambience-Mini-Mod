package me.molybdenum.ambience_mini.engine.shared.music.streams;

import java.io.IOException;
import java.io.InputStream;

public class StreamPreAllocBuffer extends LazyPreAllocBuffer {
    private final InputStream stream;
    private final int loadChunkSize;


    public StreamPreAllocBuffer(InputStream stream, int bufferSize, int loadChunkSize) {
        super(bufferSize);
        this.stream = stream;
        this.loadChunkSize = loadChunkSize;
    }


    @Override
    protected synchronized void loadBufferTo(int targetPosition) throws IOException {
        int bytesToLoad = Math.min(
                (((targetPosition - bytesLoaded) / loadChunkSize) + 1) * loadChunkSize,
                bufferSize - bytesLoaded
        );
        int len;
        do {
            if ((len = stream.read(buffer, bytesLoaded, bytesToLoad)) <= 0)
                break;
            bytesToLoad -= len;
            bytesLoaded += len;
        } while (bytesToLoad != 0);
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
