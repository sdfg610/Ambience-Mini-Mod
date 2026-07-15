package me.molybdenum.ambience_mini.engine.client.music.decoders;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.music.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.music.misc.TagReader;
import me.molybdenum.ambience_mini.engine.shared.utils.Deferred;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


public abstract class AmDecoder {
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static final HashMap<String, Function<MusicInstance, AmDecoder>> FILETYPE_TO_DECODER = new HashMap<>();

    protected final Music music;
    protected final Deferred<TagReader> tagReader;


    protected AmDecoder(Music music, Deferred<TagReader> tagReader) {
        this.music = music;
        this.tagReader = tagReader;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract AudioFormat getFormat();
    @Nullable public abstract ByteBuffer getFrame();

    public abstract void close();


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public String getMusicPath() {
        return music.path();
    }

    @Nullable
    public String getMusicTitle() {
        return tagReader.get().getTitle();
    }

    @Nullable
    public String getMusicAuthor() {
        return tagReader.get().getAuthor();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Static API
    public static AmDecoder of(MusicInstance mInst) {
        init();
        var constructor = FILETYPE_TO_DECODER.get(mInst.music().getExtension());
        if (constructor != null)
            return constructor.apply(mInst);

        throw new RuntimeException("Unsupported file format '" + mInst.music().getExtension() + "'! How did this get through the semantic analysis? Or is there another bug?");
    }

    public static Set<String> getSupportedFileTypes() {
        init();
        return FILETYPE_TO_DECODER.keySet();
    }

    public static boolean isSupportedFileType(String fileType) {
        return getSupportedFileTypes().contains(fileType);
    }


    private static void init() {
        if (isInitialized.compareAndSet(false, true)) {
            FILETYPE_TO_DECODER.put("mp3", MP3Decoder::new);
            FILETYPE_TO_DECODER.put("flac", FlacDecoder::new);
            FILETYPE_TO_DECODER.put("ogg", OggDecoder::new);
        }
    }
}
