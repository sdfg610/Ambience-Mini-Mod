package me.molybdenum.ambience_mini.engine;

public enum AmLang
{
    // Global
    MOD_NAME("mod_name"),

    // Keys
    KEY_RELOAD("key.reload"),
    KEY_NEXT_MUSIC("key.next_music"),
    KEY_PRINT_ALL("key.print_all"),

    // Toasts
    TOAST_RELOAD("toast.reload_description"),
    TOAST_NEXT_MUSIC("toast.next_music_description"),
    TOAST_PRINTING_ALL("toast.printing_all"),

    TOAST_HAS_SERVER_SUPPORT("toast.has_server_support"),
    TOAST_NO_SERVER_SUPPORT("toast.no_server_support")

    ;

    public final String key;

    AmLang(String key) {
        this.key = key;
    }
}
