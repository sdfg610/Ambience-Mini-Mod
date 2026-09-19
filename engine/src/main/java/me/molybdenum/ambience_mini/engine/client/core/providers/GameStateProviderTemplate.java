package me.molybdenum.ambience_mini.engine.client.core.providers;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.McVersion;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GameStateProviderTemplate extends BaseGameStateProvider
{
    // Global events
    public static final EventTemplate E_MAIN_MENU = new EventTemplate("main_menu", instance -> instance::inMainMenu);
    public static final EventTemplate E_JOINING = new EventTemplate("joining", instance -> instance::isJoiningWorld);
    public static final EventTemplate E_DISCONNECTED = new EventTemplate("disconnected", instance -> instance::isDisconnected);
    public static final EventTemplate E_CREDITS = new EventTemplate("credits", instance -> instance::onCreditsScreen);
    public static final EventTemplate E_PAUSED = new EventTemplate("paused", instance -> instance::isPaused);
    public static final EventTemplate E_IN_GAME = new EventTemplate("in_game", instance -> instance::inGame);

    // Time events
    public static final EventTemplate E_DAY = new EventTemplate("day", instance -> instance::isDay);
    public static final EventTemplate E_DAWN = new EventTemplate("dawn", instance -> instance::isDawn);
    public static final EventTemplate E_DUSK = new EventTemplate("dusk", instance -> instance::isDusk);
    public static final EventTemplate E_NIGHT = new EventTemplate("night", instance -> instance::isNight);

    // Weather events
    public static final EventTemplate E_DOWNFALL = new EventTemplate("downfall", instance -> instance::isDownfall);
    public static final EventTemplate E_RAIN = new EventTemplate("rain", instance -> instance::isRaining);
    public static final EventTemplate E_SNOW = new EventTemplate("snow", instance -> instance::isSnowing);
    public static final EventTemplate E_THUNDERING = new EventTemplate("thunder", instance -> instance::isThundering);

    // Location events
    public static final EventTemplate E_VILLAGE = new EventTemplate("village", 1000, instance -> instance::inVillage);
    public static final EventTemplate E_RANCH = new EventTemplate("ranch", 1000, instance -> instance::inRanch);

    // Player state events
    public static final EventTemplate E_DEAD = new EventTemplate("dead", instance -> instance::isDead);
    public static final EventTemplate E_SLEEPING = new EventTemplate("sleeping", instance -> instance::isSleeping);
    public static final EventTemplate E_FISHING = new EventTemplate("fishing", instance -> instance::isFishing);
    public static final EventTemplate E_UNDER_WATER = new EventTemplate("under_water", instance -> instance::isUnderWater);
    public static final EventTemplate E_IN_LAVA = new EventTemplate("in_lava", instance -> instance::inLava);
    public static final EventTemplate E_ON_FIRE = new EventTemplate("on_fire", instance -> instance::onFire);
    public static final EventTemplate E_IN_POWDER_SNOW = new EventTemplate("in_powder_snow", instance -> instance::inPowderSnow);
    public static final EventTemplate E_DROWNING = new EventTemplate("drowning", instance -> instance::isDrowning);

    // Mount events
    public static final EventTemplate E_MINECART = new EventTemplate("minecart", instance -> instance::inMinecart);
    public static final EventTemplate E_BOAT = new EventTemplate("boat", instance -> instance::inBoat);
    public static final EventTemplate E_HORSE = new EventTemplate("horse", instance -> instance::onHorse);
    public static final EventTemplate E_DONKEY = new EventTemplate("donkey", instance -> instance::onDonkey);
    public static final EventTemplate E_PIG = new EventTemplate("pig", instance -> instance::onPig);
    public static final EventTemplate E_ELYTRA = new EventTemplate("elytra", instance -> instance::flyingElytra);

    // Combat events
    public static final EventTemplate E_WARDEN_NEARBY = new EventTemplate("warden_nearby", 1000, instance -> instance::wardenNearby, McVersion.V1_19);
    public static final EventTemplate E_IS_TARGETED = new EventTemplate("is_targeted", instance -> instance::isTargeted);
    public static final EventTemplate E_IS_FIGHTING = new EventTemplate("is_fighting", instance -> instance::isFighting);
    public static final EventTemplate E_IN_COMBAT = new EventTemplate("in_combat", instance -> instance::inCombat);
    public static final EventTemplate E_BOSS_FIGHT = new EventTemplate("boss_fight", instance -> instance::inBossFight);

    public static final EventTemplate[] EVENTS = new EventTemplate[] {
            E_MAIN_MENU, E_JOINING, E_DISCONNECTED, E_CREDITS, E_PAUSED, E_IN_GAME,
            E_DAY, E_DAWN, E_DUSK, E_NIGHT,
            E_DOWNFALL, E_RAIN, E_SNOW, E_THUNDERING,
            E_VILLAGE, E_RANCH,
            E_DEAD, E_SLEEPING, E_FISHING, E_UNDER_WATER, E_IN_LAVA, E_ON_FIRE, E_IN_POWDER_SNOW, E_DROWNING,
            E_MINECART, E_BOAT, E_HORSE, E_DONKEY, E_PIG, E_ELYTRA,
            E_WARDEN_NEARBY, E_IS_TARGETED, E_IS_FIGHTING, E_IN_COMBAT, E_BOSS_FIGHT
    };


    // Global properties
    public static final PropertyTemplate P_SCREEN_ID = new PropertyTemplate("screen_id", new StringT(), instance -> instance::getMenuID);
    public static final PropertyTemplate P_MENU = new PropertyTemplate("menu", new StringT(), instance -> instance::getMenuType);

    // World properties
    public static final PropertyTemplate P_DIFFICULTY = new PropertyTemplate("difficulty", new StringT(), instance -> instance::getDifficulty);
    public static final PropertyTemplate P_DIMENSION = new PropertyTemplate("dimension", new StringT(), instance -> instance::getDimensionId);
    public static final PropertyTemplate P_BIOME = new PropertyTemplate("biome", new StringT(), instance -> instance::getBiomeId);
    public static final PropertyTemplate P_BIOME_TAGS = new PropertyTemplate("biome_tags", new ListT(new StringT()), instance -> instance::getBiomeTagIDs);
    public static final PropertyTemplate P_TIME = new PropertyTemplate("time", new IntT(), instance -> instance::getTime);
    public static final PropertyTemplate P_CAVE_SCORE = new PropertyTemplate("cave_score", new FloatT(), instance -> instance::getCaveScore);
    public static final PropertyTemplate P_SKYLIGHT_SCORE = new PropertyTemplate("skylight_potential", new FloatT(), instance -> instance::getSkylightScore);

    // Location properties
    public static final PropertyTemplate P_AREAS = new PropertyTemplate("areas", new ListT(new AreaT()), instance -> instance::getIntersectingAreas);
    public static final PropertyTemplate P_STRUCTURES = new PropertyTemplate("structures", new ListT(new StringT()), instance -> instance::getIntersectingStructures);

    // Player properties
    public static final PropertyTemplate P_UUID = new PropertyTemplate("uuid", new StringT(), instance -> instance::getPlayerUUID);
    public static final PropertyTemplate P_GAME_MODE = new PropertyTemplate("game_mode", new StringT(), instance -> instance::getGameMode);
    public static final PropertyTemplate P_HEALTH = new PropertyTemplate("health", new FloatT(), instance -> instance::getPlayerHealth);
    public static final PropertyTemplate P_MAX_HEALTH = new PropertyTemplate("max_health", new FloatT(), instance -> instance::getPlayerMaxHealth);
    public static final PropertyTemplate P_ELEVATION = new PropertyTemplate("elevation", new FloatT(), instance -> instance::getPlayerElevation);
    public static final PropertyTemplate P_VEHICLE = new PropertyTemplate("vehicle", new StringT(), instance -> instance::getVehicleId);
    public static final PropertyTemplate P_EFFECTS = new PropertyTemplate("effects", new ListT(new StringT()), instance -> instance::getActiveEffects);

    // Combat properties
    public static final PropertyTemplate P_COMBATANT_COUNT = new PropertyTemplate("combatant_count", new IntT(), instance -> instance::countCombatants);
    public static final PropertyTemplate P_COMBATANTS = new PropertyTemplate("combatants", new ListT(new CombatantT()), instance -> instance::getCombatants);
    public static final PropertyTemplate P_BOSS = new PropertyTemplate("boss", new StringT(), instance -> instance::getBoss);
    public static final PropertyTemplate P_BOSSES = new PropertyTemplate("bosses", new ListT(new StringT()), instance -> instance::getBosses);

    // Entity properties
    public static final PropertyTemplate P_LIVING = new PropertyTemplate("nearby_living", new ListT(new LivingT()), 1000, instance -> instance::getNearbyLiving);

    // Flag properties
    public static final PropertyTemplate P_FLAGS = new PropertyTemplate("flags", new MapT(new StringT(), new StringT()), instance -> instance::getFlags);

    // Server playlists properties
    public static final PropertyTemplate P_SERVER_PLAYLISTS = new PropertyTemplate("server_playlists", new MapT(new StringT(), new PlaylistT()), instance -> instance::getServerPlaylists);

    public static final PropertyTemplate[] PROPERTIES = new PropertyTemplate[] {
            P_SCREEN_ID, P_MENU,
            P_DIFFICULTY, P_DIMENSION, P_BIOME, P_BIOME_TAGS, P_TIME, P_CAVE_SCORE, P_SKYLIGHT_SCORE,
            P_AREAS, P_STRUCTURES,
            P_UUID, P_GAME_MODE, P_HEALTH, P_MAX_HEALTH, P_ELEVATION, P_VEHICLE, P_EFFECTS,
            P_COMBATANT_COUNT, P_COMBATANTS, P_BOSS, P_BOSSES,
            P_LIVING,
            P_FLAGS,
            P_SERVER_PLAYLISTS
    };


    public GameStateProviderTemplate(McVersion currentVersion)
    {
        for (var event : EVENTS)
            if (currentVersion.greaterThanOrEqual(event.minimumMcVersion)) {
                var getter = event.getter.apply(this);
                registerEvent(
                        event.name,
                        event.delay
                                .map(delay -> withDelay(delay, getter))
                                .orElse(getter)
                );
            }

        for (var property : PROPERTIES)
            if (currentVersion.greaterThanOrEqual(property.minimumMcVersion)) {
                var getter = property.getter.apply(this);
                registerProperty(
                        property.name,
                        property.type,
                        property.delay
                                .map(delay -> withDelay(delay, getter))
                                .orElse(getter)
                );
            }
    }

    protected <T> Supplier<T> withDelay(long delay, Supplier<T> getter) {
        return new Supplier<>() {
            private long latestTime = 0;
            private T latestVal;

            @Override
            public T get() {
                long now = System.currentTimeMillis();
                if (now - latestTime > delay) {
                    latestTime = now;
                    latestVal = getter.get();
                }
                return latestVal;
            }
        };
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    public abstract BoolVal inMainMenu();
    public abstract BoolVal isJoiningWorld();
    public abstract BoolVal isDisconnected();
    public abstract BoolVal onCreditsScreen();
    public abstract BoolVal isPaused();
    public abstract BoolVal inGame();


    // ------------------------------------------------------------------------------------------------
    // Time events
    public abstract BoolVal isDay();
    public abstract BoolVal isDawn();
    public abstract BoolVal isDusk();
    public abstract BoolVal isNight();


    // ------------------------------------------------------------------------------------------------
    // Weather events
    public abstract BoolVal isDownfall();
    public abstract BoolVal isRaining();
    public abstract BoolVal isSnowing();
    public abstract BoolVal isThundering();


    // ------------------------------------------------------------------------------------------------
    // Location events
    public abstract BoolVal inVillage();
    public abstract BoolVal inRanch();


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    public abstract BoolVal isDead();
    public abstract BoolVal isSleeping();
    public abstract BoolVal isFishing();
    public abstract BoolVal isUnderWater();
    public abstract BoolVal inLava();
    public abstract BoolVal onFire();
    public abstract BoolVal inPowderSnow();
    public abstract BoolVal isDrowning();


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    public abstract BoolVal inMinecart();
    public abstract BoolVal inBoat();
    public abstract BoolVal onHorse();
    public abstract BoolVal onDonkey();
    public abstract BoolVal onPig();
    public abstract BoolVal flyingElytra();


    // ------------------------------------------------------------------------------------------------
    // Combat events
    public abstract BoolVal wardenNearby();
    public abstract BoolVal isTargeted();
    public abstract BoolVal isFighting();
    public abstract BoolVal inCombat();
    public abstract BoolVal inBossFight();



    // ------------------------------------------------------------------------------------------------
    // World properties
    public abstract StringVal getMenuID();
    public abstract StringVal getMenuType();


    // ------------------------------------------------------------------------------------------------
    // World properties
    public abstract StringVal getDifficulty();
    public abstract StringVal getDimensionId();
    public abstract StringVal getBiomeId();
    public abstract ListVal getBiomeTagIDs();
    public abstract IntVal getTime();
    public abstract FloatVal getCaveScore();
    public abstract FloatVal getSkylightScore();


    // ------------------------------------------------------------------------------------------------
    // Location properties
    public abstract ListVal getIntersectingAreas();
    public abstract ListVal getIntersectingStructures();


    // ------------------------------------------------------------------------------------------------
    // Player properties
    public abstract StringVal getPlayerUUID();
    public abstract StringVal getGameMode();
    public abstract FloatVal getPlayerHealth();
    public abstract FloatVal getPlayerMaxHealth();
    public abstract FloatVal getPlayerElevation();
    public abstract StringVal getVehicleId();
    public abstract ListVal getActiveEffects();


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    public abstract IntVal countCombatants();
    public abstract ListVal getCombatants();
    public abstract StringVal getBoss();
    public abstract ListVal getBosses();


    // ------------------------------------------------------------------------------------------------
    // Entity properties
    public abstract Value<?> getNearbyLiving();


    // ------------------------------------------------------------------------------------------------
    // Flag properties
    public abstract MapVal getFlags();


    // ------------------------------------------------------------------------------------------------
    // Server playlists properties
    public abstract Value<?> getServerPlaylists();



    public record EventTemplate(
            String name,
            Optional<Long> delay,
            Function<GameStateProviderTemplate, Supplier<BoolVal>> getter,
            McVersion minimumMcVersion
    ) {
        public EventTemplate(String name, Function<GameStateProviderTemplate, Supplier<BoolVal>> getter) {
            this(name, Optional.empty(), getter, McVersion.V1_18);
        }

        public EventTemplate(String name, long delay, Function<GameStateProviderTemplate, Supplier<BoolVal>> getter) {
            this(name, Optional.of(delay), getter, McVersion.V1_18);
        }

        public EventTemplate(String name, long delay, Function<GameStateProviderTemplate, Supplier<BoolVal>> getter, McVersion minimumMcVersion) {
            this(name, Optional.of(delay), getter, minimumMcVersion);
        }
    }

    public record PropertyTemplate(
            String name,
            Type type,
            Optional<Long> delay,
            Function<GameStateProviderTemplate, Supplier<Value<?>>> getter,
            McVersion minimumMcVersion
    ) {
        public PropertyTemplate(String name, Type type, Function<GameStateProviderTemplate, Supplier<Value<?>>> getter) {
            this(name, type, Optional.empty(), getter, McVersion.V1_18);
        }

        public PropertyTemplate(String name, Type type, long delay, Function<GameStateProviderTemplate, Supplier<Value<?>>> getter) {
            this(name, type, Optional.of(delay), getter, McVersion.V1_18);
        }
    }
}
