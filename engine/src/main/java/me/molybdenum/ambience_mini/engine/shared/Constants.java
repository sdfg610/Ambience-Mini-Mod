package me.molybdenum.ambience_mini.engine.shared;

import java.nio.file.Path;

public class Constants
{
    // Mod
    public static final String MOD_ID = "ambience_mini";

    // Configuration
    public static final Path musicPackPath = Path.of("ambience_music");
    public static final Path musicDirPath = musicPackPath.resolve("music");
    public static final Path musicConfigPath = musicPackPath.resolve("music_config.txt");

    // Storage
    public static final String AM_STORAGE_DIRECTORY = "am_storage";
    public static final String AM_LOCAL_STORAGE_DIRECTORY = "am_local";

    // Areas
    public static final float AREA_SELECTION_RANGE = 48f;
    public static final float AREA_LINE_WIDTH = 2f;
    public static final int MAX_AREA_NAME_LENGTH = 50;

    // Flags
    public static final int MAX_FLAG_ID_LENGTH = 50;
    public static final int MAX_FLAG_VALUE_LENGTH = 100;

    // Warden
    public static final int WARDEN_SEARCH_RADIUS = 32; // 2 chunks

    // Networking
    public static final String PROTOCOL_VERSION = "1"; // Should never actually change to preserve backwards compatibility

    // Remote Music
    public static final int PLAYLIST_CHUNK_BYTE_SIZE = 1024*3; // 3 KiB
    public static final int MUSIC_CHUNK_BYTE_SIZE = 1024*3; // 3 KiB

    // Size measures
    public static final int BYTES_IN_MEGABYTE = 1_000_000;
}
