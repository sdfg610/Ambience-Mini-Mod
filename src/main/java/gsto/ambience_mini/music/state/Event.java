package gsto.ambience_mini.music.state;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

public class Event {
    public static final ArrayList<Event> EVENTS = new ArrayList<>();


    public final String name;
    private final Supplier<Boolean> isActive;

    public Event(String name, Supplier<Boolean> isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive.get();
    }


    public static Optional<Event> get(String name) {
        return EVENTS.stream().filter(event -> event.name.equals(name)).findFirst();
    }

    public static boolean exists(String name) {
        return EVENTS.stream().anyMatch(event -> event.name.equals(name));
    }

    public static Event register(String name, Supplier<Boolean> isActive) {
        if (EVENTS.stream().anyMatch(ev -> ev.name.equals(name)))
            throw new RuntimeException("Duplicate registration of AmbienceMini-event: " + name);

        Event ev = new Event(name, isActive);
        EVENTS.add(ev);
        return ev;
    }

    // Global events
    public static final Event MAIN_MENU = register("main_menu", GameStateMonitor::inMainMenu);
    public static final Event JOINING = register("joining", GameStateMonitor::isJoiningWorld);
    public static final Event IN_GAME = register("in_game", GameStateMonitor::inGame);
    public static final Event CREDITS = register("credits", () -> false);

    // Time
    public static final Event DAY = register("day", () -> false);
    public static final Event DAWN = register("dawn", () -> false);
    public static final Event DUSK = register("dusk", () -> false);
    public static final Event NIGHT = register("night", () -> false);

    // Weather
    public static final Event DOWNFALL = register("downfall", () -> false);
    public static final Event RAIN = register("rain", () -> false);
    public static final Event SNOW = register("snow", () -> false);

    // Special locations
    public static final Event VILLAGE = register("village", () -> false);
    public static final Event RANCH = register("ranch", () -> false);

    // Height-based
    public static final Event UNDER_DEEPSLATE = register("under_deepslate", () -> false);
    public static final Event UNDERGROUND = register("underground", () -> false);
    public static final Event UNDERWATER = register("under_water", () -> false);
    public static final Event HIGH_UP = register("high_up", () -> false);

    // Player state
    public static final Event DYING = register("dying", () -> false);
    public static final Event DEAD = register("dead", () -> false);
    public static final Event SLEEPING = register("sleeping", () -> false);
    public static final Event FISHING = register("fishing", () -> false);

    // Mounts
    public static final Event MINECART = register("minecart", () -> false);
    public static final Event BOAT = register("boat", () -> false);
    public static final Event HORSE = register("horse", () -> false);
    public static final Event DONKEY = register("donkey", () -> false);
    public static final Event PIG = register("pig", () -> false);
    public static final Event FLYING_ELYTRA = register("flying_elytra", () -> false);

    // Combat
    public static final Event IN_COMBAT = register("in_combat", () -> false);
    public static final Event BOSS_FIGHT = register("boss_fight", () -> false);
    public static final Event RAID = register("raid", () -> false);
}
