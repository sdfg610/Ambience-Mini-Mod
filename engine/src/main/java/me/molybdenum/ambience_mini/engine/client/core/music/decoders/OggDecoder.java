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
    private static final int BUFFER_SIZE = 400_000;

    private final byte[] miniBuffer = new byte[VorbisFile.CHUNKSIZE];
    private final byte[] buffer = new byte[BUFFER_SIZE + VorbisFile.CHUNKSIZE];
    private int currentLength = 0;

    private final VorbisFile file;
    private final AudioFormat format;

    private final int sampleByteSize;


    public OggDecoder(MusicInstance inst) {
        super(inst);
        try {
            // Thanks to: https://github.com/tulskiy/musique/blob/master/musique-core/src/main/java/com/tulskiy/musique/audio/formats/ogg/VorbisDecoder.java
            file = new VorbisFile(ensureLoopableIfNeeded(inst.createStream(), inst.getMusicSize(), inst.music.doLoop()));
            Info info = file.getInfo()[0];
            sampleByteSize = 2 * info.channels;
            format = new AudioFormat(info.rate, 16, info.channels, true, false);
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
        while (currentLength < BUFFER_SIZE && loadFrameToBuffer()) {
            long samples = file.pcm_tell();
            if (samples >= loopEnd) {
                currentLength -= (int)(samples - loopEnd)*sampleByteSize;
                file.pcm_seek(loopStart);
                loadFrameToBuffer();
            }
        }
        if (currentLength <= 0)
            return null;

        ByteBuffer buf = BufferUtils.createByteBuffer(currentLength);
        buf.put(buffer, 0, currentLength);
        buf.rewind();

        currentLength = 0;

        return buf;
    }

    private boolean loadFrameToBuffer() {
        int len = file.read(miniBuffer, VorbisFile.CHUNKSIZE);
        System.arraycopy(miniBuffer, 0, buffer, currentLength, len);
        currentLength += len;
        return len != 0;
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
