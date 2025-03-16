package gsto.ambience_mini.music.state;

import java.util.ArrayList;

public class Event {
    public static final ArrayList<Event> EVENTS = new ArrayList<>();


    public final String name;

    public Event(String name) {
        this.name = name;
    }

    public static Event register(String name) {
        Event ev = new Event(name);
        EVENTS.add(ev);
        return ev;
    }


    // Global events
    public static final Event MAIN_MENU = register("main_menu");
    public static final Event JOINING = register("joining");
    public static final Event CREDITS = register("credits");
    public static final Event IN_GAME = register("in_game");

    // Time
    public static final Event DAY = register("day");
    public static final Event DAWN = register("dawn");
    public static final Event DUSK = register("dusk");
    public static final Event NIGHT = register("night");

    // Weather
    public static final Event DOWNFALL = register("downfall");
    public static final Event RAIN = register("rain");
    public static final Event SNOW = register("snow");

    // Special locations
    public static final Event VILLAGE = register("village");
    public static final Event RANCH = register("ranch");

    // Height-based
    public static final Event UNDER_DEEPSLATE = register("under_deepslate");
    public static final Event UNDERGROUND = register("underground");
    public static final Event UNDERWATER = register("under_water");
    public static final Event HIGH_UP = register("high_up");

    // Player state
    public static final Event DYING = register("dying");
    public static final Event DEAD = register("dead");
    public static final Event SLEEPING = register("sleeping");
    public static final Event FISHING = register("fishing");

    // Mounts
    public static final Event MINECART = register("minecart");
    public static final Event BOAT = register("boat");
    public static final Event HORSE = register("horse");
    public static final Event DONKEY = register("donkey");
    public static final Event PIG = register("pig");
    public static final Event FLYING_ELYTRA = register("flying_elytra");

    // Combat
    public static final Event IN_COMBAT = register("in_combat");
    public static final Event BOSS_FIGHT = register("boss_fight");
    public static final Event RAID = register("raid");
}
