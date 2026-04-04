package me.molybdenum.ambience_mini.engine.shared;

public class Common
{
    // Mod
    public static final String MOD_ID = "ambience_mini";

    // Storage
    public static final String AM_STORAGE_DIRECTORY = "am_storage";
    public static final String NAME_CACHE_FILE_NAME = "name_cache.json";

    // Configuration
    public static final String AMBIENCE_MUSIC_DIRECTORY = "ambience_music";
    public static final String MUSIC_DIRECTORY = "music";
    public static final String MUSIC_CONFIG_FILE = "music_config.txt";

    // Areas
    public static final float AREA_SELECTION_RANGE = 48f;
    public static final float AREA_LINE_WIDTH = 2f;
    public static final int MAX_AREA_NAME_LENGTH = 50;   // TODO: Check and enforce this everywhere!

    // Warden
    public static final int WARDEN_SEARCH_RADIUS = 32; // 2 chunks

    // Networking
    public static final String PROTOCOL_VERSION = "1"; // Should never actually change to preserve backwards compatibility
}
