package me.molybdenum.ambience_mini.engine.shared.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.*;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class Utils {
    public static String padToLength(String str, int length) {
        return String.format("%-" + length + "s", str);
    }

    public static <T> String getKeyValuePairString(List<Pair<String, T>> pairs) {
        int maxKeyLength = pairs.stream()
                .map(pair -> pair.left().length())
                .max(Integer::compareTo)
                .orElse(0);

        StringBuilder sb = new StringBuilder();
        var it = pairs.iterator();
        while (it.hasNext()) {
            var pair = it.next();

            sb.append(' ');
            sb.append(Utils.padToLength(pair.left(), maxKeyLength));
            sb.append(" = ");
            sb.append(pair.right().toString());

            if (it.hasNext())
                sb.append('\n');
        }

        return sb.toString();
    }


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

    public static String removeIllegalCharacters(String name) {
        return name.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isJsonObjectWith(JsonElement elem, String... fields) {
        if (!elem.isJsonObject())
            return false;

        JsonObject obj = elem.getAsJsonObject();
        return Arrays.stream(fields).allMatch(obj::has);
    }

    public static boolean isJsonBoolean(JsonElement elem) {
        return elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isBoolean();
    }

    public static boolean isJsonNumber(JsonElement elem) {
        return elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber();
    }

    public static boolean isJsonString(JsonElement elem) {
        return elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString();
    }


    public static boolean isFalse(Boolean b) {
        return b != null && !b;
    }

    public static boolean isTrue(Boolean b) {
        return b != null && b;
    }


    public static void ignoreException(Runnable task) {
        try {
            task.run();
        } catch (Exception ignored) { }
    }


    public static void printMessages(Logger logger, List<Message> messages) {
        for (var error : messages) {
            if (error instanceof SynError err)
                logger.error("Syntactic error [line {}, column {}]: {}", err.line(), err.column(), err.message());
            else if (error instanceof SemError err)
                logger.error("Semantic error [line {}]: {}", err.line(), err.message());
            else if (error instanceof Warning wrn)
                logger.warn("Warning [line {}]: {}", wrn.line(), wrn.message());
            else if (error instanceof ExcError err)
                logger.error("An exception occurred while loading the music configuration:\n", err.exception());
            else
                throw new RuntimeException("Could not print error of type: " + error.getClass().getName());
        }
    }
}
