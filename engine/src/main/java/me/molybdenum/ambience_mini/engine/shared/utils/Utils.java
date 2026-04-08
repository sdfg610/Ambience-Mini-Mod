package me.molybdenum.ambience_mini.engine.shared.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
}
