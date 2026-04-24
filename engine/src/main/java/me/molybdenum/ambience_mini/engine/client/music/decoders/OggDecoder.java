package me.molybdenum.ambience_mini.engine.client.music.decoders;

import com.jcraft_am_custom.jorbis.Info;
import com.jcraft_am_custom.jorbis.JOrbisException;
import com.jcraft_am_custom.jorbis.VorbisFile;
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
    private final byte[] buffer = new byte[BUFFER_SIZE + VorbisFile.CHUNKSIZE]; // Part after plus allows the latest frame to overflow the buffer. Handled later
    private int currentLength = 0;

    private final VorbisFile file;
    private final AudioFormat format;


    public OggDecoder(InputStream stream) {
        try {
            // Thanks to: https://github.com/tulskiy/musique/blob/master/musique-core/src/main/java/com/tulskiy/musique/audio/formats/ogg/VorbisDecoder.java
            file = new VorbisFile(stream, null, 0);
            Info info = file.getInfo()[0];
            format = new AudioFormat(info.rate, 16, info.channels, true, false);
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
        int len;
        while (currentLength < BUFFER_SIZE && (len = file.read(miniBuffer, VorbisFile.CHUNKSIZE)) > 0) {
            System.arraycopy(miniBuffer, 0, buffer, currentLength, len);
            currentLength += len;
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


    @Override
    public void close() {
        try {
            file.close();
        } catch (IOException ignored) {
            // ignored
        }
    }
}
