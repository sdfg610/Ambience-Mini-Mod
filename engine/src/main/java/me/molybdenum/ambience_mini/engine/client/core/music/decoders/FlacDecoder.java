package me.molybdenum.ambience_mini.engine.client.core.music.decoders;

import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.utils.Deferred;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.Nullable;
import me.molybdenum.ambience_mini.deps.jflac.FLACDecoder;
import me.molybdenum.ambience_mini.deps.jflac.frame.Frame;
import me.molybdenum.ambience_mini.deps.jflac.metadata.Metadata;
import me.molybdenum.ambience_mini.deps.jflac.metadata.StreamInfo;
import me.molybdenum.ambience_mini.deps.jflac.metadata.VorbisComment;
import me.molybdenum.ambience_mini.deps.jflac.util.ByteData;
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

    public FlacDecoder(MusicInstance mInst) {
        super(mInst);
        try {
            this.stream = ensureLoopableIfNeeded(mInst.createStream(), mInst.music.doLoop());
            this.decoder = new FLACDecoder(stream);

            StreamInfo streamInfo = getStreamInfo(decoder.readMetadata());
            format = streamInfo.getAudioFormat();
            maxFrameSize = streamInfo.getMaxFrameSize();

            sampleByteSize = info.getBytesInSample();
            buffer = new byte[BUFFER_SIZE + 2*(maxFrameSize * sampleByteSize)];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream ensureLoopableIfNeeded(InputStream stream, boolean requireMark) {
        return requireMark && !stream.markSupported()
                ? new BufferedInputStream(stream)
                : stream;
    }


    public static MusicInfo getMusicInfo(MusicInstance inst) {
        try (var stream = inst.createStream()) {
            var metadata = new FLACDecoder(stream).readMetadata();
            var streamInfo = getStreamInfo(metadata);
            var comment = getTags(metadata);

            var bytesInSample = (streamInfo.getBitsPerSample() / 8) * streamInfo.getChannels();
            return new MusicInfo(
                    tryGetTag("title", comment), tryGetTag("artist", comment),
                    streamInfo.getSampleRate(), bytesInSample, streamInfo.getTotalSamples(),
                    Utils.tryParseLong(tryGetTag("loopstart", comment)),
                    Utils.tryParseLong(tryGetTag("loopend", comment)),
                    Utils.tryParseLong(tryGetTag("looplength", comment))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private static String tryGetTag(String key, VorbisComment comment) {
        return Arrays.stream(comment.getCommentByName(key)).findFirst().orElse(null);
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
}
