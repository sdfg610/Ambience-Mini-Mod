package me.molybdenum.ambience_mini.engine.client.music.decoders;

import javazoom.jlayer_am_custom.decoder.*;
import me.molybdenum.ambience_mini.engine.client.music.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.music.misc.TagReader;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Function;

public class MP3Decoder extends AmDecoder {
    private static final int BUFFER_SIZE = 200_000; // Double the number measured in bytes since using "short"

    private final long bufferMaxSamples;

    private final short[] buffer = new short[BUFFER_SIZE + Obuffer.OBUFFERSIZE*2];
    private int currentShortLength = 0;

    private final Decoder decoder = new Decoder();
    private final Bitstream bitstream;
    private boolean hitLast = false;

    private final long loopStart;
    private final long loopEnd;

    private final int sampleShortSize;
    private long samplesDecoded;
    private long savedSamplesDecoded = -1;



    public MP3Decoder(MusicInstance mInst) {
        try {
            boolean doLoop = mInst.music().loop();

            bitstream = new Bitstream(ensureLoopableIfNeeded(mInst.createStream(), doLoop));
            decoder.initialize(bitstream.readFrame()); // Load metadata
            bitstream.unreadFrame();

            sampleShortSize = decoder.getOutputChannels(); // JLayer always produces samples of 2 bytes or 1 short.
            bufferMaxSamples = Obuffer.OBUFFERSIZE / sampleShortSize;

            if (doLoop) {
                var startAndEnd = new MP3TagReader(bitstream.getID3v2Tags()).getLoopStartAndEnd();
                loopStart = startAndEnd.left();
                loopEnd = startAndEnd.right();
            }
            else
                loopStart = loopEnd = Long.MAX_VALUE;
        } catch (IOException | BitstreamException | DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream ensureLoopableIfNeeded(InputStream stream, boolean requireMark) {
        return requireMark && !stream.markSupported()
                ? new BufferedInputStream(stream)
                : stream;
    }


    @Override
    public AudioFormat getFormat() {
        return new AudioFormat((float)decoder.getOutputFrequency(),
                16,
                decoder.getOutputChannels(),
                true,
                false);
    }

    @Override
    public @Nullable ByteBuffer getFrame() {
        try {
            while (currentShortLength < BUFFER_SIZE && readFrameToBuffer(0)) {
                if (samplesDecoded >= loopEnd) {
                    currentShortLength -= (int)(samplesDecoded - loopEnd)*sampleShortSize; // Find overflow in bytes (not short)
                    bitstream.restoreState();
                    decoder.restoreState();
                    samplesDecoded = savedSamplesDecoded;
                    readFrameToBuffer((int)(loopStart-samplesDecoded));
                }
            }
            if (currentShortLength <= 0)
                return null;

            // Create new buffer to return
            ByteBuffer buf = BufferUtils.createByteBuffer(currentShortLength*2);
            for (int i = 0; i < currentShortLength; ++i)
                buf.putShort(buffer[i]);
            buf.rewind();

            currentShortLength = 0;

            return buf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes a single frame.
     * @return true if there are more frames to decode, false otherwise.
     */
    private boolean readFrameToBuffer(int skipSamples) throws JavaLayerException {
        if (hitLast)
            return false;

        if (loopStart != Long.MAX_VALUE && samplesDecoded >= loopStart-bufferMaxSamples && samplesDecoded <= loopStart) {
            bitstream.saveState();
            decoder.saveState();
            savedSamplesDecoded = samplesDecoded;
        }

        Header h = bitstream.readFrame();
        if (h == null) {
            hitLast = true;
            return false;
        }
        SampleBuffer data = (SampleBuffer)decoder.decodeFrame(h, bitstream);
        bitstream.closeFrame();

        int trueSkip = skipSamples * sampleShortSize;
        int size = data.getBufferLength() - trueSkip;
        System.arraycopy(data.getBuffer(), trueSkip, buffer, currentShortLength, size);
        currentShortLength += size;
        samplesDecoded += size / sampleShortSize;

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


    private static class MP3TagReader extends TagReader {
        private final Map<String, String> tags;


        private MP3TagReader(Map<String, String> comment) {
            this.tags = comment;
        }


        @Override
        public String getLoopStartStr() {
            return getCaseInsensitive("loopstart");
        }

        @Override
        public String getLoopEndStr() {
            return getCaseInsensitive("loopend");
        }

        @Override
        public String getLoopLengthStr() {
            return getCaseInsensitive("looplength");
        }


        private String getCaseInsensitive(String key) {
            for (var entry : tags.entrySet())
                if (entry.getKey().toLowerCase().equals(key))
                    return entry.getValue();
            return null;
        }
    }
}
