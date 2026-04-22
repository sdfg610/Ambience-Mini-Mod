package me.molybdenum.ambience_mini.engine.client.music.decoders;

import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


public abstract class AmDecoder {
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    protected static final HashMap<String, Function<InputStream, AmDecoder>> FILETYPE_TO_DECODER = new HashMap<>();


    public abstract AudioFormat getFormat();

    @Nullable
    public abstract ByteBuffer getFrame();

    public abstract void close();


    public static AmDecoder of(String format, InputStream stream) {
        init();
        var constructor = FILETYPE_TO_DECODER.get(format);
        if (constructor != null)
            return constructor.apply(stream);

        throw new RuntimeException("Unsupported file format '" + format + "'! How did this get through the semantic analysis? Or is there another bug?");
    }

    public static Set<String> getSupportedFileTypes() {
        init();
        return FILETYPE_TO_DECODER.keySet();
    }


    private static void init() {
        if (isInitialized.compareAndSet(false, true)) {
            FILETYPE_TO_DECODER.put("mp3", MP3Decoder::new);
            FILETYPE_TO_DECODER.put("flac", FlacDecoder::new);
            FILETYPE_TO_DECODER.put("ogg", OggDecoder::new);
        }
    }
}
