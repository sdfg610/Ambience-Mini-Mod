package me.molybdenum.ambience_mini.engine.shared;

import me.molybdenum.ambience_mini.engine.shared.utils.Text;

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
    MSG_PLAYER_CRASHED("message.player_crashed", 1),
    MSG_RELOAD("message.reload_description"),
    MSG_NEXT_MUSIC("message.next_music_description"),
    MSG_PRINTING_ALL("message.printing_all"),
    MSG_NOT_RUNNING("message.not_running"),

    MSG_FULL_SERVER_SUPPORT("message.full_server_support"),
    MSG_PARTIAL_SERVER_SUPPORT("message.partial_server_support"),
    MSG_NO_SERVER_SUPPORT("message.no_server_support"),

    MSG_PAUSING_MUSIC("message.pausing_music"),
    MSG_RESUMING_MUSIC("message.resuming_music"),

    MSG_AREA_VIEW_OFF("message.area_view_off"),
    MSG_AREA_SELECTOR_ENABLED("message.area_selector_enabled"),
    MSG_AREA_CONSTRUCTOR_ENABLED("message.area_constructor_enabled"),
    MSG_AREA_INVALID_LOCATION("message.area_invalid_location"),
    MSG_AREA_LOOK_AT_DESTINATION("message.area_look_at_destination"),
    MSG_AREA_CANNOT_EDIT("message.area_cannot_edit"),
    MSG_AREA_NAME_REQUIREMENTS("message.area_name_requirements", 1), // Args: "max length"

    MSG_FLAG_ALREADY_EXISTS("message.flag_already_exists", 1), // Args: "id"
    MSG_FLAG_NOT_EXISTS("message.flag_not_exists", 1), // Args: "id"
    MSG_FLAG_CREATED("message.flag_created", 2), // Args: "id", "value"
    MSG_FLAG_UPDATED("message.flag_updated", 2), // Args: "id", "value"
    MSG_FLAG_DELETED("message.flag_deleted", 1), // Args: "id"
    MSG_FLAG_ID_INVALID("message.flag_id_invalid", 2), // Args: "id", "max length"
    MSG_FLAG_VALUE_INVALID("message.flag_value_invalid", 2), // Args: "value", "max length"
    MSG_FLAG_NAME_REQUIREMENTS("message.flag_name_requirements", 1), // Args: "max length"
    MSG_FLAG_VALUE_REQUIREMENTS("message.flag_value_requirements", 1), // Args: "max length"

    MSG_UNHANDLED_MESSAGE("message.unhandled_message"),
    MSG_UNHANDLED_CLIENT_MESSAGE("message.unhandled_client_message"),
    MSG_MESSAGE_CAUSED_SERVER_ERROR("message.message_caused_server_error"),

    MSG_INVALID_ARGUMENT("message.invalid_argument", 1),

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
    public final int argCount;


    AmLang(String key, int argCount) {
        this.key = key;
        this.argCount = argCount;
    }

    AmLang(String key) {
        this(key, 0);
    }


    public Text text(String... args) {
        return Text.ofTranslatable(this, args);
    }
}
