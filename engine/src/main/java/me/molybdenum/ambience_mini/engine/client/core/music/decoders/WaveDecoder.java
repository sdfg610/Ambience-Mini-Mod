package me.molybdenum.ambience_mini.engine.client.core.music.decoders;

import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.utils.Deferred;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// Thank you: https://github.com/s4l4x/audio-analysis/blob/master/src/com/badlogic/audio/io/WaveDecoder.java
public class WaveDecoder extends AmDecoder
{
    private static final int BUFFER_SAMPLE_CAPACITY = 50_000;

    private final EndianDataInputStream stream;

    private final int channels;
    private final int sampleRate;
    private final int sampleBitSize;
    private final int sampleByteSize;

    private final int bufferSize;
    private final byte[] buffer;

    private final int numSamples;
    private int samplesRead = 0;

    private final int loopStart;
    private final int loopEnd;

    private int loopState = 0; // 0 -> loop disabled. 1 -> loopStart not found, 2 -> loopStart found.


    public WaveDecoder(MusicInstance mInst) {
        super(mInst.music(), new Deferred<>(new WavTagReader(mInst.music())));
        try {
            boolean doLoop = mInst.music().doLoop();

            // RIFF
            stream = new EndianDataInputStream(ensureLoopableIfNeeded(mInst.createStream(), doLoop));
            if( !stream.read4ByteString().equals( "RIFF" ) )
                throw new RuntimeException( "Could not find 'RIFF' tag" );

            stream.readIntLittleEndian(); // Skip file size

            // WAVE
            if( !stream.read4ByteString().equals( "WAVE" ) )
                throw new RuntimeException( "Could not find 'WAVE' tag");
            if( !stream.read4ByteString().equals( "fmt " ) )
                throw new RuntimeException( "Could not find 'fmt' tag" );
            if( stream.readIntLittleEndian() != 16 )
                throw new RuntimeException( "Expected wave chunk size to be '16'" );
            if( stream.readShortLittleEndian() != 1 )
                throw new RuntimeException( "Expected format to be '1'" );

            channels = stream.readShortLittleEndian();
            sampleRate = stream.readIntLittleEndian();
            stream.readIntLittleEndian(); // Skip "bytes per second"
            stream.readShortLittleEndian(); // Skip "block align"
            sampleBitSize = stream.readShortLittleEndian();

            if (sampleBitSize != 8 && sampleBitSize != 16)
                throw new RuntimeException( "Sample bit size must be '8' or '16'" );

            // DATA
            if( !stream.read4ByteString().equals( "data" ) )
                throw new RuntimeException( "Could not find 'data' tag" );

            sampleByteSize = channels * (sampleBitSize / 8);
            numSamples = stream.readIntLittleEndian() / sampleByteSize;

            bufferSize = sampleByteSize * BUFFER_SAMPLE_CAPACITY;
            buffer = new byte[bufferSize];

            if (doLoop) {
                var startAndEnd = tagReader.get().getLoopStartAndEnd();
                loopStart = startAndEnd.left().intValue();
                loopEnd = startAndEnd.right().intValue();
                loopState = 1;
            }
            else
                loopStart = loopEnd = Integer.MAX_VALUE;
        } catch (IOException e) {
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
        return new AudioFormat(
                (float)sampleRate,
                sampleBitSize,
                channels,
                sampleBitSize == 16, // 16 bit is signed. 8 bit is unsigned.
                false
        );
    }

    @Override
    public @Nullable ByteBuffer getFrame() {
        try {
            var length = loadToBuffer(0);
            if (length == 0)
                return null;

            var buf = BufferUtils.createByteBuffer(length);
            buf.put(buffer, 0, length);
            buf.rewind();
            return buf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int loadToBuffer(int loadedBytes) throws IOException {
        if (loadedBytes >= bufferSize || samplesRead >= numSamples || stream.available() <= 0)
            return loadedBytes;

        if (loopState == 1 && samplesRead + BUFFER_SAMPLE_CAPACITY >= loopStart) {
            int length = readSamples(loadedBytes, (loopStart - samplesRead) * sampleByteSize);
            stream.mark(Integer.MAX_VALUE);
            loopState = 2;
            return loadToBuffer(loadedBytes + length);
        }
        else if (loopState == 2 && samplesRead + BUFFER_SAMPLE_CAPACITY >= loopEnd) {
            int length = readSamples(loadedBytes, (loopEnd - samplesRead) * sampleByteSize);
            stream.reset();
            samplesRead = loopStart;
            return loadToBuffer(loadedBytes + length);
        }
        else
            return loadedBytes + readSamples(loadedBytes, bufferSize - loadedBytes);
    }

    private int readSamples(int offset, int length) throws IOException {
        int remainingBytes = (numSamples - samplesRead) * sampleByteSize;
        int bytesRead = stream.read(buffer, offset, Math.min(length, remainingBytes));
        samplesRead += bytesRead / sampleByteSize;
        return bytesRead;
    }


    @Override
    public void close() {
        try {
            stream.close();
        } catch (Exception ignored) {
            // Ignored
        }
    }


    private static class WavTagReader extends TagReader {
        private WavTagReader(Music music) {
            super(music);
        }


        @Override
        public String getLoopStartStr() {
            return null;
        }

        @Override
        public String getLoopEndStr() {
            return null;
        }

        @Override
        public String getLoopLengthStr() {
            return null;
        }

        @Override
        public @Nullable String getTitle() {
            return null;
        }

        @Override
        public @Nullable String getAuthor() {
            return null;
        }
    }


    // Thank you: https://github.com/s4l4x/audio-analysis/blob/master/src/com/badlogic/audio/io/EndianDataInputStream.java
    private static class EndianDataInputStream extends DataInputStream {
        public EndianDataInputStream(InputStream in) {
            super(in);
        }

        public String read4ByteString( ) throws IOException {
            byte[] bytes = new byte[4];
            readFully(bytes);
            return new String( bytes, StandardCharsets.US_ASCII);
        }

        public short readShortLittleEndian( ) throws IOException {
            int result = readUnsignedByte();
            result |= readUnsignedByte() << 8;
            return (short)result;
        }

        public int readIntLittleEndian( ) throws IOException {
            int result = readUnsignedByte();
            result |= readUnsignedByte() << 8;
            result |= readUnsignedByte() << 16;
            result |= readUnsignedByte() << 24;
            return result;
        }
    }
}
