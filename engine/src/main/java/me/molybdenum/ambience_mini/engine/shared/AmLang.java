package me.molybdenum.ambience_mini.engine.shared;

public enum AmLang
{
    // Global
    MOD_NAME("mod_name"),

    // Keys
    KEY_RELOAD("key.reload"),
    KEY_PLAY_PAUSE("key.play_pause"),
    KEY_NEXT_MUSIC("key.next_music"),
    KEY_PRINT_ALL("key.print_all"),
    KEY_TOGGLE_AREAS("key.toggle_areas"),
    KEY_AREA_CONFIRM("key.area_confirm"),
    KEY_AREA_CANCEL("key.area_cancel"),

    // Messages
    MSG_RELOAD("message.reload_description"),
    MSG_NEXT_MUSIC("message.next_music_description"),
    MSG_PRINTING_ALL("message.printing_all"),

    MSG_FULL_SERVER_SUPPORT("message.full_server_support"),
    MSG_PARTIAL_SERVER_SUPPORT("message.partial_server_support"),
    MSG_OUTDATED_VERSION_ON_SERVER("message.partial_server_support"),
    MSG_NO_SERVER_SUPPORT("message.no_server_support"),

    MSG_PAUSING_MUSIC("message.pausing_music"),
    MSG_RESUMING_MUSIC("message.resuming_music"),

    MSG_AREA_VIEW_OFF("message.area_view_off"),
    MSG_AREA_SELECTOR_ENABLED("message.area_selector_enabled"),
    MSG_AREA_CONSTRUCTOR_ENABLED("message.area_constructor_enabled"),
    MSG_AREA_INVALID_LOCATION("message.area_invalid_location"),
    MSG_AREA_LOOK_AT_DESTINATION("message.area_look_at_destination"),
    MSG_AREA_CANNOT_EDIT("message.area_cannot_edit"),

    MSG_PLAYER_CRASHED("message.player_crashed"),
    MSG_UNHANDLED_MESSAGE("message.unhandled_message"),
    MSG_UNHANDLED_CLIENT_MESSAGE("message.unhandled_client_message"),
    MSG_MESSAGE_CAUSED_SERVER_ERROR("message.message_caused_server_error"),

    STRING_AREA_NAME("string.area_name"),
    STRING_OWNERSHIP_AND_SHARING("string.ownership_and_sharing"),
    STRING_PRIVATE("string.private"),
    STRING_SHARED("string.shared"),
    STRING_PUBLIC("string.public"),
    STRING_LOCAL("string.local"),
    STRING_SAVE("string.save"),
    STRING_CANCEL("string.cancel"),
    STRING_EDIT_BOUNDS("string.edit_bounds"),
    STRING_DELETE("string.delete")

    ;

    public final String key;

    AmLang(String key) {
        this.key = key;
    }
}
