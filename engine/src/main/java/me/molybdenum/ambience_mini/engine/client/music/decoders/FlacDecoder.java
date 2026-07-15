package me.molybdenum.ambience_mini.engine.client.music.decoders;

import me.molybdenum.ambience_mini.engine.client.music.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.music.misc.TagReader;
import me.molybdenum.ambience_mini.engine.shared.utils.Deferred;
import org.jetbrains.annotations.Nullable;
import org.jflac_am_custom.FLACDecoder;
import org.jflac_am_custom.frame.Frame;
import org.jflac_am_custom.metadata.Metadata;
import org.jflac_am_custom.metadata.StreamInfo;
import org.jflac_am_custom.metadata.VorbisComment;
import org.jflac_am_custom.util.ByteData;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class FlacDecoder extends AmDecoder
{
    private static final int BUFFER_SIZE = 400_000;

    private final int sampleByteSize;

    private final int maxFrameSize;
    private final byte[] buffer;
    private int currentLength = 0;

    private final InputStream stream;
    private final FLACDecoder decoder;
    private final AudioFormat format;

    private final long loopStart;
    private final long loopEnd;


    public FlacDecoder(MusicInstance mInst) {
        super(mInst.music(), new Deferred<>());
        try {
            boolean doLoop = mInst.music().loop();

            this.stream = ensureLoopableIfNeeded(mInst.createStream(), doLoop);
            this.decoder = new FLACDecoder(stream);

            Metadata[] metadata = decoder.readMetadata();
            tagReader.set(new FlacTagReader(getTags(metadata)));

            StreamInfo streamInfo = getStreamInfo(metadata);
            format = streamInfo.getAudioFormat();
            sampleByteSize = (streamInfo.getBitsPerSample() / 8) * streamInfo.getChannels();
            maxFrameSize = streamInfo.getMaxFrameSize();

            int maxFrameByteSize = maxFrameSize * sampleByteSize;
            buffer = new byte[BUFFER_SIZE + 2*maxFrameByteSize];

            if (doLoop) {
                var startAndEnd = tagReader.get().getLoopStartAndEnd();
                loopStart = startAndEnd.left();
                loopEnd = startAndEnd.right();
            }
            else
                loopStart = loopEnd = Long.MAX_VALUE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream ensureLoopableIfNeeded(InputStream stream, boolean requireMark) {
        return requireMark && !stream.markSupported()
                ? new BufferedInputStream(stream)
                : stream;
    }


    private static StreamInfo getStreamInfo(Metadata[] metadata) {
        for (Metadata meta : metadata)
            if (meta instanceof StreamInfo si)
                return si;
        throw new RuntimeException("Malformed flac file: StreamInfo metadata block missing");
    }

    private static VorbisComment getTags(Metadata[] metadata) {
        for (Metadata meta : metadata)
            if (meta instanceof VorbisComment comment)
                return comment;
        throw new RuntimeException("Malformed flac file: Comment metadata block missing");
    }


    @Override
    public AudioFormat getFormat() {
        return format;
    }


    @Override
    public @Nullable ByteBuffer getFrame() {
        try {
            while (currentLength < BUFFER_SIZE && readFrameToBuffer(0)) {
                if (decoder.getSamplesDecoded() >= loopEnd) {
                    currentLength -= (int)(decoder.getSamplesDecoded() - loopEnd)*sampleByteSize;
                    decoder.restoreState();
                    readFrameToBuffer((int)(loopStart-decoder.getSamplesDecoded()));
                }
            }
            if (currentLength <= 0)
                return null;

            // Create new buffer to return
            ByteBuffer buf = BufferUtils.createByteBuffer(currentLength);
            buf.put(buffer, 0, currentLength);
            buf.rewind();

            currentLength = 0;

            return buf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes a single frame.
     * @return true if there are more frames to decode, false otherwise.
     */
    private boolean readFrameToBuffer(int skipSamples) throws IOException {
        if (decoder.isEOF())
            return false;

        if (loopStart != Long.MAX_VALUE && decoder.getSamplesDecoded() >= loopStart-maxFrameSize && decoder.getSamplesDecoded() <= loopStart) {
            decoder.saveState();
        }

        Frame frame = decoder.readNextFrame();
        if (frame == null)
            return false;

        ByteData data = decoder.decodeFrame(frame, null);
        int trueSkip = skipSamples * sampleByteSize;
        int size = data.getLen() - trueSkip;
        System.arraycopy(data.getData(), trueSkip, buffer, currentLength, size);
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


    private static class FlacTagReader extends TagReader {
        private final VorbisComment comment;


        private FlacTagReader(VorbisComment comment) {
            this.comment = comment;
        }


        @Override
        public String getLoopStartStr() {
            return Arrays.stream(comment.getCommentByName("loopstart")).findFirst().orElse(null);
        }

        @Override
        public String getLoopEndStr() {
            return Arrays.stream(comment.getCommentByName("loopend")).findFirst().orElse(null);
        }

        @Override
        public String getLoopLengthStr() {
            return Arrays.stream(comment.getCommentByName("looplength")).findFirst().orElse(null);
        }

        @Override
        public @Nullable String getTitle() {
            return Arrays.stream(comment.getCommentByName("title")).findFirst().orElse(null);
        }

        @Override
        public @Nullable String getAuthor() {
            return Arrays.stream(comment.getCommentByName("artist")).findFirst().orElse(null);
        }
    }
}
