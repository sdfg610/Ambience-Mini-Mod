package me.molybdenum.ambience_mini.engine.client.core.music.decoders;

import me.molybdenum.ambience_mini.deps.jid3lib.MP3File;
import me.molybdenum.ambience_mini.deps.jid3lib.TagException;
import me.molybdenum.ambience_mini.deps.jid3lib.id3.AbstractID3v2;
import me.molybdenum.ambience_mini.deps.jlayer.*;
import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class MP3Decoder extends AmDecoder {
    private static final int BUFFER_SIZE = 200_000; // Double the number measured in bytes since using "short"

    private final long bufferMaxSamples;

    private final short[] buffer = new short[BUFFER_SIZE + Obuffer.OBUFFERSIZE*2];
    private int currentShortLength = 0;

    private final Decoder decoder = new Decoder();
    private final Bitstream bitstream;
    private boolean hitLast = false;

    private final int sampleShortSize;
    private long samplesDecoded;
    private long savedSamplesDecoded = -1;

    private final AudioFormat format;



    public MP3Decoder(MusicInstance inst) {
        super(inst);
        try {
            boolean doLoop = inst.music.doLoop();

            bitstream = new Bitstream(ensureLoopableIfNeeded(inst.createStream(), doLoop));
            decoder.initialize(bitstream.readFrame()); // Load metadata
            bitstream.unreadFrame();

            sampleShortSize = decoder.getOutputChannels(); // JLayer always produces samples of 2 bytes or 1 short per channel.
            bufferMaxSamples = Obuffer.OBUFFERSIZE / sampleShortSize;

            format = new AudioFormat(
                    (float)decoder.getOutputFrequency(),
                    16,
                    decoder.getOutputChannels(),
                    true,
                    false
            );
        } catch (IOException | BitstreamException | DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream ensureLoopableIfNeeded(InputStream stream, boolean requireMark) {
        return requireMark && !stream.markSupported()
                ? new BufferedInputStream(stream)
                : stream;
    }


    public static MusicInfo getMusicInfo(MusicInstance inst) {
        try (var stream = new BufferedInputStream(inst.createStream())) {
            var bitstream = new Bitstream(stream);
            var decoder = new Decoder();
            decoder.initialize(bitstream.readFrame());
            bitstream.unreadFrame();

            int shortsInSample = decoder.getOutputChannels(); // num channels * 1 short

            Header h;
            long samples = 0;
            while ((h = bitstream.readFrame()) != null && !Thread.interrupted()) {
                SampleBuffer data = (SampleBuffer)decoder.decodeFrame(h, bitstream);
                bitstream.closeFrame();
                samples += data.getBufferLength() / shortsInSample;
            }

            String title = null, author = null;
            Long loopStart = null, loopEnd = null, loopLength = null;
            try {
                MP3File mp3file = new MP3File(inst.getLocalPath().toFile());
                if (mp3file.hasID3v2Tag()) {
                    AbstractID3v2 tag = mp3file.getID3v2Tag();
                    title = tag.getSongTitle();
                    author = tag.getLeadArtist();
                    loopStart = tag.getTXXXValue("LOOPSTART").map(Utils::tryParseLong).orElse(null);
                    loopEnd = tag.getTXXXValue("LOOPEND").map(Utils::tryParseLong).orElse(null);
                    loopLength = tag.getTXXXValue("LOOPLENGTH").map(Utils::tryParseLong).orElse(null);
                }
            } catch (Exception e) {
                BaseAmbienceMini.getBaseCore().logger.error("Error during MP3 tags parsing", e);
            }

            return new MusicInfo(
                    title, author,
                    decoder.getOutputFrequency(), 2*decoder.getOutputChannels(), samples,
                    loopStart, loopEnd, loopLength
            );
        } catch (IOException | BitstreamException | DecoderException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public AudioFormat getFormat() {
        return format;
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
        } catch (Exception ignored) { }
    }
}
