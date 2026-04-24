package me.molybdenum.ambience_mini.engine.client.music.decoders;

import org.jetbrains.annotations.Nullable;
import org.jflac_am_custom.FLACDecoder;
import org.jflac_am_custom.frame.Frame;
import org.jflac_am_custom.metadata.Metadata;
import org.jflac_am_custom.metadata.StreamInfo;
import org.jflac_am_custom.util.ByteData;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class FlacDecoder extends AmDecoder
{
    private static final int BUFFER_SIZE = 400_000;

    private final int maxFrameSize;
    private final byte[] buffer;
    private int currentLength = 0;

    private final InputStream stream;
    private final FLACDecoder decoder;
    private final AudioFormat format;


    public FlacDecoder(InputStream stream) {
        this.stream = stream;
        this.decoder = new FLACDecoder(stream);

        Metadata md;
        try {
            md = Arrays.stream(decoder.readMetadata())
                    .filter(m -> m instanceof StreamInfo)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (md instanceof StreamInfo streamInfo) {
            format = streamInfo.getAudioFormat();
            maxFrameSize = streamInfo.getMaxFrameSize() * streamInfo.getBitsPerSample();
            buffer = new byte[BUFFER_SIZE + maxFrameSize]; // Part after plus allows the latest frame to overflow the buffer. Handled later
        }
        else
            throw new RuntimeException("There is no stream-info at the beginning of music file!");
    }


    @Override
    public AudioFormat getFormat() {
        return format;
    }


    @Override
    public @Nullable ByteBuffer getFrame() {
        try {
            //noinspection StatementWithEmptyBody
            while (currentLength < BUFFER_SIZE && readFrameToBuffer()) {
                // Just read until buffer is full or no more data.
            }
            if (currentLength <= 0)
                return null;

            // Create new buffer to return
            int len = Math.min(BUFFER_SIZE, currentLength);
            ByteBuffer buf = BufferUtils.createByteBuffer(len);
            buf.put(buffer, 0, len);
            buf.rewind();

            // Move surplus data to start of "buffer"
            System.arraycopy(buffer, BUFFER_SIZE, buffer, 0, maxFrameSize);
            currentLength -= BUFFER_SIZE; // If this goes negative, we are out of audio data anyway, so no problem.

            return buf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes a single frame.
     * @return true if there are more frames to decode, false otherwise.
     */
    private boolean readFrameToBuffer() throws IOException {
        if (decoder.isEOF())
            return false;

        Frame frame = decoder.readNextFrame();
        if (frame == null)
            return false;

        ByteData data = decoder.decodeFrame(frame, null);
        int size = data.getLen();
        System.arraycopy(data.getData(), 0, buffer, currentLength, size);
        currentLength += size;

        return true;
    }

    @Override
    public void close() {
        try {
            stream.close();
        } catch (IOException ignored) {
            // ignored
        }
    }
}
