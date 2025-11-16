package me.molybdenum.ambience_mini.engine.utils;

import java.util.List;

public class Utils {
    public static final List<String> SUPPORTED_FILE_TYPES = List.of("mp3", "flac");

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
}
