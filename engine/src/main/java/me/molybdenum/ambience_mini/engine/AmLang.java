package me.molybdenum.ambience_mini.engine;

public enum AmLang
{
    // Global
    MOD_NAME("mod_name"),

    // Keys
    KEY_RELOAD("key.reload"),
    KEY_PLAY_PAUSE("key.play_pause"),
    KEY_NEXT_MUSIC("key.next_music"),
    KEY_PRINT_ALL("key.print_all"),

    // Messages
    MSG_RELOAD("message.reload_description"),
    MSG_NEXT_MUSIC("message.next_music_description"),
    MSG_PRINTING_ALL("message.printing_all"),

    MSG_HAS_SERVER_SUPPORT("message.has_server_support"),
    MSG_NO_SERVER_SUPPORT("message.no_server_support"),

    MSG_PAUSING_MUSIC("message.pausing_music"),
    MSG_RESUMING_MUSIC("message.resuming_music"),

    MSG_PLAYER_CRASHED("message.player_crashed")

    ;

    public final String key;

    AmLang(String key) {
        this.key = key;
    }
}
