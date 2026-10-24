package me.molybdenum.ambience_mini.engine.client.core.music.decoders;

import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.utils.Deferred;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
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
    private static final HashMap<String, Function<MusicInstance, MusicInfo>> FILETYPE_TO_INFO_READER = new HashMap<>();

    public final MusicInstance inst;
    public final MusicInfo info;

    protected final long loopStart;
    protected final long loopEnd;


    protected AmDecoder(MusicInstance inst) {
        this.inst = inst;
        this.info = inst.getMusicInfoBlocking();

        if (inst.music.loop()) {
            var startAndEnd = getLoopStartAndEnd();
            loopStart = startAndEnd.left();
            loopEnd = startAndEnd.right();
        }
        else
            loopStart = loopEnd = Long.MAX_VALUE;
    }

    public long getLoopStart() {
        return loopStart;
    }

    public long getLoopEnd() {
        return loopEnd;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract AudioFormat getFormat();
    @Nullable public abstract ByteBuffer getFrame();

    public abstract void close();


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    protected Pair<Long, Long> getLoopStartAndEnd()
    {
        Long loopStart = getIfNonNegativeOrElse(inst.music.loopstart(), info.getLoopStart());
        if (loopStart == null)
            throw new RuntimeException("No (valid) 'loopstart' tag in config or metadata.");
        else if (loopStart < 0)
            throw new RuntimeException("The 'loopstart' tag must be non-negative. Got: " + loopStart);

        Long loopEnd = getIfNonNegativeOrElse(inst.music.loopend(), info.getLoopEnd());
        Long loopLength = getIfNonNegativeOrElse(inst.music.looplength(), info.getLoopLength());
        if (loopEnd != null) {
            if (loopEnd <= loopStart)
                throw new RuntimeException("The 'loopstart' tag must be less than the 'loopend' tag. Got: " + loopStart + " and " + loopEnd);
        }
        else if (loopLength != null) {
            if (loopLength <= 0)
                throw new RuntimeException("The 'looplength' tag must be positive. Got: " + loopLength);
            loopEnd = loopStart + loopLength;
        }
        else
            throw new RuntimeException("No (valid) 'loopend' or 'looplength' tag in config or metadata.");

        return new Pair<>(loopStart, loopEnd);
    }

    private static Long getIfNonNegativeOrElse(long fromMusic, Long fromInfo) {
        return fromMusic >= 0 ? (Long)fromMusic : fromInfo;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Static API
    public static AmDecoder decoderOf(MusicInstance inst) {
        init();
        var constructor = FILETYPE_TO_DECODER.get(inst.music.getExtension());
        if (constructor != null)
            return constructor.apply(inst);

        throw new RuntimeException("Unsupported file format '" + inst.music.getExtension() + "'! How did this get through the semantic analysis? Or is there another bug?");
    }

    public static MusicInfo infoOf(MusicInstance inst) {
        init();
        var reader = FILETYPE_TO_INFO_READER.get(inst.music.getExtension());
        if (reader != null)
            return reader.apply(inst);

        throw new RuntimeException("Unsupported file format '" + inst.music.getExtension() + "'! How did this get through the semantic analysis? Or is there another bug?");
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
            FILETYPE_TO_DECODER.put("wav", WaveDecoder::new);

            FILETYPE_TO_INFO_READER.put("mp3", MP3Decoder::getMusicInfo);
            FILETYPE_TO_INFO_READER.put("flac", FlacDecoder::getMusicInfo);
            FILETYPE_TO_INFO_READER.put("ogg", OggDecoder::getMusicInfo);
            FILETYPE_TO_INFO_READER.put("wav", WaveDecoder::getMusicInfo);
        }
    }
}
