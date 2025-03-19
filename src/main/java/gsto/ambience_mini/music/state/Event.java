package gsto.ambience_mini.music.state;

import java.util.ArrayList;
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


    public static Event get(String name) {
        return EVENTS.stream()
                .filter(event -> event.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not get event: " + name));
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
    public static final Event DISCONNECTED = register("disconnected", GameStateMonitor::isDisconnected);
    //public static final Event PAUSED = register("paused", GameStateMonitor::isPaused); // TODO: Paused should only work in single player
    public static final Event IN_GAME = register("in_game", GameStateMonitor::inGame);
    public static final Event CREDITS = register("credits", GameStateMonitor::onCreditsScreen);

    // Time events
    public static final Event DAY = register("day",  () -> {
        int time = GameStateMonitor.getTime();   // "12542" is the time when beds can be used.
        return time > 23500 || time <= 12500;    // "23460" is the time from when beds cannot be used.
    });
    public static final Event DAWN = register("dawn",  () -> {
        int time = GameStateMonitor.getTime();
        return time > 23500 || time <= 2000;
    });
    public static final Event DUSK = register("dusk",  () -> {
        int time = GameStateMonitor.getTime();
        return time > 10300 && time <= 12500;
    });
    public static final Event NIGHT = register("night", () -> {
        int time = GameStateMonitor.getTime();
        return time > 12500 && time <= 23500;
    });

    // Weather
    public static final Event DOWNFALL = register("downfall", GameStateMonitor::isRaining);
    public static final Event RAIN = register("rain", () -> GameStateMonitor.isRaining() && !GameStateMonitor.isColdEnoughToSnow());
    public static final Event SNOW = register("snow", () -> GameStateMonitor.isRaining() && GameStateMonitor.isColdEnoughToSnow());
    public static final Event THUNDER = register("thunder", GameStateMonitor::isThundering);

    // Special locations
    public static final Event VILLAGE = register("village", GameStateMonitor::inVillage);
    public static final Event RANCH = register("ranch", GameStateMonitor::inRanch);

    // Height-based
    public static final Event UNDER_DEEPSLATE = register("under_deepslate", () -> GameStateMonitor.getPlayerElevation() < 0);
    public static final Event UNDERGROUND = register("underground", GameStateMonitor::isUnderground);
    public static final Event UNDERWATER = register("under_water", GameStateMonitor::isUnderWater);
    public static final Event HIGH_UP = register("high_up", () -> GameStateMonitor.getPlayerElevation() > GameStateMonitor.HIGH_UP_THRESHOLD);

    // Player state
    public static final Event DEAD = register("dead", GameStateMonitor::isDead);
    public static final Event SLEEPING = register("sleeping", GameStateMonitor::isSleeping);
    public static final Event FISHING = register("fishing", GameStateMonitor::isFishing);

    // Mounts
    public static final Event MINECART = register("minecart", GameStateMonitor::inMinecart);
    public static final Event BOAT = register("boat", GameStateMonitor::inBoat);
    public static final Event HORSE = register("horse", GameStateMonitor::onHorse);
    public static final Event DONKEY = register("donkey", GameStateMonitor::onDonkey);
    public static final Event PIG = register("pig", GameStateMonitor::onPig);
    //public static final Event FLYING_ELYTRA = register("flying_elytra", () -> false); // TODO: Find out how to do this in 1.20.1

    // Combat
    public static final Event IN_COMBAT = register("in_combat", () -> false);
    public static final Event BOSS_FIGHT = register("boss_fight", () -> !GameStateMonitor.getBossId().isEmpty());
    //public static final Event RAID = register("raid", () -> false); // TODO: Can be done using "getBossId".
}
