package me.molybdenum.ambience_mini.engine.client.music.decoders;

import javazoom.jlayer_am_custom.decoder.*;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MP3Decoder extends AmDecoder {
    private static final int BUFFER_SIZE = 50_000; // 100_000 bytes since using "short"

    private final short[] buffer = new short[BUFFER_SIZE + Obuffer.OBUFFERSIZE]; // Allow latest frame to overflow the buffer. Handled later
    private int currentLength = 0;

    private final Decoder decoder = new Decoder();
    private final Bitstream bitstream;



    public MP3Decoder(InputStream stream) {
        try {
            bitstream = new Bitstream(stream);
            readFrameToBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public AudioFormat getFormat() {
        // It appears that JLayer sums the frequency across all channels, but open AL does not expect that. Thus, I divide by the number fo channels.
        return new AudioFormat((float)decoder.getOutputFrequency() / decoder.getOutputChannels(),
                16,
                decoder.getOutputChannels(),
                true,
                false);
    }

    @Override
    public @Nullable ByteBuffer getFrame() {
        try {
            //noinspection StatementWithEmptyBody
            while (currentLength < BUFFER_SIZE && readFrameToBuffer()) {
                // Just read until buffer is full or no more data.
            }
            if (currentLength == 0)
                return null;

            // Create new buffer to return
            int len = Math.min(BUFFER_SIZE, currentLength);
            ByteBuffer buf = BufferUtils.createByteBuffer(len);
            for (int i = 0; i < len; ++i)
                buf.putShort(buffer[i++]);
            buf.rewind();

            // Move surplus data to start of "buffer"
            System.arraycopy(buffer, BUFFER_SIZE, buffer, 0, Obuffer.OBUFFERSIZE);
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
    private boolean readFrameToBuffer() throws JavaLayerException
    {
        Header h = bitstream.readFrame();
        if (h == null)
            return false;

        SampleBuffer data = (SampleBuffer) decoder.decodeFrame(h, bitstream);
        int size = data.getBufferLength();
        System.arraycopy(data.getBuffer(), 0, buffer, currentLength, size);
        currentLength += size;

        bitstream.closeFrame();
        return true;
    }

    @Override
    public void close() {
        try {
            bitstream.close();
        } catch (Exception ignored) {
            // Ignored
        }
    }
}
