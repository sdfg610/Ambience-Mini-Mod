package me.molybdenum.ambience_mini.engine.client.core.state;

public enum ScreenType {
    NONE("none"),
    MAIN_MENU("main_menu"),
    JOINING("joining"),
    DISCONNECTED("disconnected"),
    CREDITS("credits"),
    DEATH("death"),
    PAUSE("pause"),
    PLAYER_INVENTORY("player_inventory"),
    UNKNOWN("unknown")

    ;

    public final String name;

    ScreenType(String name) {
        this.name = name;
    }
}