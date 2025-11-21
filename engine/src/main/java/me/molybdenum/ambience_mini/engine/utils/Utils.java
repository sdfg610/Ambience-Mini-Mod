package me.molybdenum.ambience_mini.engine.utils;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Utils {
    public static String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    public static String padToLength(String str, int length) {
        return String.format("%-" + length + "s", str);
    }

    public static <A, B> Stream<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream
                .range(0, Math.min(as.size(), bs.size()))
                .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)));
    }
}
