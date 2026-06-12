package me.molybdenum.ambience_mini.engine.client.music.decoders;

import com.jcraft_am_custom.jorbis.Comment;
import com.jcraft_am_custom.jorbis.Info;
import com.jcraft_am_custom.jorbis.JOrbisException;
import com.jcraft_am_custom.jorbis.VorbisFile;
import me.molybdenum.ambience_mini.engine.client.music.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.music.misc.TagReader;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

public class OggDecoder extends AmDecoder
{
    private static final int BUFFER_SIZE = 400_000;

    private final byte[] miniBuffer = new byte[VorbisFile.CHUNKSIZE];
    private final byte[] buffer = new byte[BUFFER_SIZE + VorbisFile.CHUNKSIZE]; // Part after plus allows the latest frame to overflow the buffer. Handled later
    private int currentLength = 0;

    private final VorbisFile file;
    private final AudioFormat format;

    private final long loopStart;
    private final long loopEnd;
    private final int sampleByteSize;


    public OggDecoder(MusicInstance mInst) {
        try {
            // Thanks to: https://github.com/tulskiy/musique/blob/master/musique-core/src/main/java/com/tulskiy/musique/audio/formats/ogg/VorbisDecoder.java
            file = new VorbisFile(mInst.getMusicPath().toString());
            Info info = file.getInfo()[0];
            format = new AudioFormat(info.rate, 16, info.channels, true, false);
            sampleByteSize = 2 * info.channels;

            if (mInst.music().loop()) {
                var startAndEnd = new OggTagReader(file.getComment()).getLoopStartAndEnd();
                loopStart = startAndEnd.left();
                loopEnd = startAndEnd.right();
            }
            else
                loopStart = loopEnd = Long.MAX_VALUE;
        } catch (JOrbisException e) {
            throw new RuntimeException(e);
        }
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

        // Move surplus data to start of "buffer"
        System.arraycopy(buffer, BUFFER_SIZE, buffer, 0, VorbisFile.CHUNKSIZE);
        currentLength -= BUFFER_SIZE; // If this goes negative, we are out of audio data anyway, so no problem.

        return buf;
    }

    private boolean loadFrameToBuffer() {
        int len = file.read(miniBuffer, VorbisFile.CHUNKSIZE);
        System.arraycopy(miniBuffer, 0, buffer, currentLength, len);
        currentLength += len;
        return len != 0;
    }


    @Override
    public void close() {
        try {
            file.close();
        } catch (IOException ignored) {
            // ignored
        }
    }


    private static class OggTagReader extends TagReader {
        private final Comment[] comment;


        private OggTagReader(Comment[] comment) {
            if (comment.length == 0)
                throw new RuntimeException("Ogg file has no comment, but comments are needed to get looping info!");
            this.comment = comment;
        }


        @Override
        public String getLoopStartStr() {
            return getByKey("loopstart");
        }

        @Override
        public String getLoopEndStr() {
            return getByKey("loopend");
        }

        @Override
        public String getLoopLengthStr() {
            return getByKey("looplength");
        }


        private String getByKey(String key) {
            for (var com : comment) {
                String value = com.getComment(key);
                if (value != null)
                    return value;
            }
            return null;
        }
    }
}
