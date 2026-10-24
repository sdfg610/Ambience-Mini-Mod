package me.molybdenum.ambience_mini.engine.shared.core.music.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;

import java.util.Optional;

public class MusicInfo
{
    public static final MusicInfo EMPTY = new MusicInfo();

    private final String title;
    private final String author;
    private final int rate;      // Samples per second
    private final int bytesInSample;
    private final long samples;  // Total samples

    private final Long loopStart;  // Total samples
    private final Long loopEnd;  // Total samples
    private final Long loopLength;  // Total samples


    public MusicInfo(
            String title, String author,
            int rate, int bytesInSample, long samples,
            Long loopStart, Long loopEnd, Long loopLength) {
        this.title = title;
        this.author = author;

        this.rate = rate;
        this.bytesInSample = bytesInSample;
        this.samples = samples;

        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
        this.loopLength = loopLength;
    }

    public MusicInfo() {
        this(
                null, null,
                -1, -1, -1L,
                -1L, -1L, -1L
        );
    }


    public String getTitleOrNull() {
        return title;
    }

    public String getAuthorOrNull() {
        return author;
    }

    public int getSampleRate() {
        return rate;
    }

    public int getBytesInSample() {
        return bytesInSample;
    }

    public long getTotalSamples() {
        return samples;
    }


    public Long getLoopStart() {
        return loopStart;
    }

    public Long getLoopEnd() {
        return loopEnd;
    }

    public Long getLoopLength() {
        return loopLength;
    }


    public long getTotalMinutes() {
        return (samples / rate) / 60;
    }

    public int getSecondsPart() {
        return (int)(samples / rate) % 60;
    }

    public String getDurationString() {
        return String.format("%02d", getTotalMinutes()) + ":" + String.format("%02d", getSecondsPart());
    }


    public JsonObject toJson(String hash) {
        JsonObject obj = new JsonObject();
        obj.add("hash", new JsonPrimitive(hash));
        obj.add("title", title != null ? new JsonPrimitive(title) : JsonNull.INSTANCE);
        obj.add("author", author != null ? new JsonPrimitive(author) : JsonNull.INSTANCE);
        obj.add("rate", new JsonPrimitive(rate));
        obj.add("bytesInSample", new JsonPrimitive(bytesInSample));
        obj.add("samples", new JsonPrimitive(samples));
        obj.add("loopStart", loopStart == null ? JsonNull.INSTANCE : new JsonPrimitive(loopStart));
        obj.add("loopEnd", loopEnd == null ? JsonNull.INSTANCE : new JsonPrimitive(loopEnd));
        obj.add("loopLength", loopLength == null ? JsonNull.INSTANCE : new JsonPrimitive(loopLength));
        return obj;
    }

    public static Optional<Pair<String, MusicInfo>> tryOfJson(JsonElement elem) {
        // Is elem an object?
        if (!elem.isJsonObject())
            return Optional.empty();

        var obj = elem.getAsJsonObject();
        try {
            return Optional.of(new Pair<>(
                    getString(obj, "hash"),
                    new MusicInfo(
                            getString(obj, "title"),
                            getString(obj, "author"),
                            getLong(obj, "rate").orElseThrow().intValue(),
                            getLong(obj, "bytesInSample").orElseThrow().intValue(),
                            getLong(obj, "samples").orElseThrow(),
                            getLong(obj, "loopStart").orElse(null),
                            getLong(obj, "loopEnd").orElse(null),
                            getLong(obj, "loopLength").orElse(null)
                    )
            ));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static String getString(JsonObject obj, String field) {
        if (!obj.has(field))
            return null;
        var elem = obj.get(field);
        if (elem.isJsonPrimitive())
            return elem.getAsString();
        if (elem.isJsonNull())
            return null;

        throw new RuntimeException();
    }

    private static Optional<Long> getLong(JsonObject obj, String field) {
        if (!obj.has(field))
            return Optional.empty();
        var elem = obj.get(field);
        if (!elem.isJsonPrimitive())
            return Optional.empty();
        var prim = elem.getAsJsonPrimitive();
        return Optional.ofNullable(prim.isNumber() ? prim.getAsLong() : null);
    }
}
