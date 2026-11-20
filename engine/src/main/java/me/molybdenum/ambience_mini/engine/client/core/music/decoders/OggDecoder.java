package me.molybdenum.ambience_mini.engine.client.core.music.decoders;

import me.molybdenum.ambience_mini.deps.jcraft.jorbis.Comment;
import me.molybdenum.ambience_mini.deps.jcraft.jorbis.Info;
import me.molybdenum.ambience_mini.deps.jcraft.jorbis.JOrbisException;
import me.molybdenum.ambience_mini.deps.jcraft.jorbis.VorbisFile;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.core.music.streams.FullyBufferedInputStream;
import me.molybdenum.ambience_mini.engine.shared.core.music.streams.StreamPreAllocBuffer;
import me.molybdenum.ambience_mini.engine.shared.utils.Deferred;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class OggDecoder extends AmDecoder
{
    private static final int SAMPLES_IN_MINI_BUFFER = 2_000; // Not a lot, no, but VorbisFile only reads at most 4096 bytes
    private static final int SAMPLES_IN_BUFFER = 200_000;

    private final byte[] miniBuffer;
    private final byte[] buffer;
    private int currentLength = 0;

    private final VorbisFile file;
    private final AudioFormat format;

    private final int sampleByteSize;
    private final int miniBufferSize;
    private final int unpaddedBufferSize;



    public OggDecoder(MusicInstance inst) {
        super(inst);
        try {
            // Thanks to: https://github.com/tulskiy/musique/blob/master/musique-core/src/main/java/com/tulskiy/musique/audio/formats/ogg/VorbisDecoder.java
            file = new VorbisFile(ensureLoopableIfNeeded(inst.createStream(), inst.getMusicSize(), inst.music.doLoop()));
            Info info = file.getInfo()[0];
            sampleByteSize = 2 * info.channels;
            format = new AudioFormat(info.rate, 16, info.channels, true, false);

            miniBuffer = new byte[SAMPLES_IN_MINI_BUFFER * sampleByteSize];
            miniBufferSize = miniBuffer.length;

            buffer = new byte[(SAMPLES_IN_BUFFER+SAMPLES_IN_MINI_BUFFER) * sampleByteSize];
            unpaddedBufferSize = SAMPLES_IN_BUFFER * sampleByteSize;
        } catch (IOException | JOrbisException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream ensureLoopableIfNeeded(InputStream stream, int musicSize, boolean doLoop) {
        return doLoop && !(stream instanceof FullyBufferedInputStream)
                ? new FullyBufferedInputStream(new StreamPreAllocBuffer(stream, musicSize, 1024*5), true)
                : stream;
    }


    @Override
    public AudioFormat getFormat() {
        return format;
    }


    @Override
    public @Nullable ByteBuffer getFrame() {
        try {
            //noinspection StatementWithEmptyBody
            while (currentLength < unpaddedBufferSize && loadFrameToBuffer()) { }
            if (currentLength <= 0)
                return null;

            ByteBuffer buf = BufferUtils.createByteBuffer(currentLength);
            buf.put(buffer, 0, currentLength);
            buf.rewind();

            currentLength = 0;

            return buf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean loadFrameToBuffer() throws IOException {
        int len;
        if (isLooping && !file.isStateSaved() && file.pcm_tell()+SAMPLES_IN_MINI_BUFFER >= loopStart) {
            len = file.read(miniBuffer, (int)(loopStart-file.pcm_tell()) * sampleByteSize);
            if (file.pcm_tell() == loopStart)
                file.saveState();
        } else if (isLooping && file.isStateSaved() && file.pcm_tell()+SAMPLES_IN_MINI_BUFFER >= loopEnd) {
            len = file.read(miniBuffer, (int)(loopEnd-file.pcm_tell()) * sampleByteSize);
            if (file.pcm_tell() == loopEnd)
                file.restoreState();
        } else {
            len = file.read(miniBuffer, miniBufferSize);
        }
        System.arraycopy(miniBuffer, 0, buffer, currentLength, len);
        currentLength += len;
        return len != 0 || isLooping;
    }


    public static MusicInfo getMusicInfo(MusicInstance inst) {
        try (var stream = inst.createStream()) {
            var file = new VorbisFile(stream);
            var fileInfo = file.getInfo()[0];

            int sampleByteSize = 2 * fileInfo.channels;
            int readSize = sampleByteSize * VorbisFile.CHUNKSIZE;

            byte[] buf = new byte[readSize];
            int len;
            long samples = 0;
            while ((len = file.read(buf, readSize)) != 0 && !Thread.interrupted())
                samples += len / sampleByteSize;

            var comments = file.getComment();
            return new MusicInfo(
                    getByKey("title", comments), getByKey("artist", comments),
                    fileInfo.rate, sampleByteSize, samples,
                    Utils.tryParseLong(getByKey("loopstart", comments)),
                    Utils.tryParseLong(getByKey("loopend", comments)),
                    Utils.tryParseLong(getByKey("looplength", comments))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getByKey(String key, Comment[] comment) {
        for (var com : comment) {
            String value = com.getComment(key);
            if (value != null)
                return value;
        }
        return null;
    }


    @Override
    public void close() {
        try {
            file.close();
        } catch (IOException ignored) {
            // ignored
        }
    }
}
