package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.*;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GameStateProviderV1Template extends BaseGameStateProvider
{
    // Global events
    public static final EventTemplateV1 E_MAIN_MENU = new EventTemplateV1("main_menu", instance -> instance::inMainMenu);
    public static final EventTemplateV1 E_JOINING = new EventTemplateV1("joining", instance -> instance::isJoiningWorld);
    public static final EventTemplateV1 E_DISCONNECTED = new EventTemplateV1("disconnected", instance -> instance::isDisconnected);
    public static final EventTemplateV1 E_CREDITS = new EventTemplateV1("credits", instance -> instance::onCreditsScreen);
    public static final EventTemplateV1 E_PAUSED = new EventTemplateV1("paused", instance -> instance::isPaused);
    public static final EventTemplateV1 E_IN_GAME = new EventTemplateV1("in_game", instance -> instance::inGame);

    // Time events
    public static final EventTemplateV1 E_DAY = new EventTemplateV1("day", instance -> instance::isDay);
    public static final EventTemplateV1 E_DAWN = new EventTemplateV1("dawn", instance -> instance::isDawn);
    public static final EventTemplateV1 E_DUSK = new EventTemplateV1("dusk", instance -> instance::isDusk);
    public static final EventTemplateV1 E_NIGHT = new EventTemplateV1("night", instance -> instance::isNight);

    // Weather events
    public static final EventTemplateV1 E_DOWNFALL = new EventTemplateV1("downfall", instance -> instance::isDownfall);
    public static final EventTemplateV1 E_RAIN = new EventTemplateV1("rain", instance -> instance::isRaining);
    public static final EventTemplateV1 E_SNOW = new EventTemplateV1("snow", instance -> instance::isSnowing);
    public static final EventTemplateV1 E_THUNDERING = new EventTemplateV1("thunder", instance -> instance::isThundering);

    // Location events
    public static final EventTemplateV1 E_VILLAGE = new EventTemplateV1("village", instance -> instance::inVillage);
    public static final EventTemplateV1 E_RANCH = new EventTemplateV1("ranch", instance -> instance::inRanch);

    // Player state events
    public static final EventTemplateV1 E_DEAD = new EventTemplateV1("dead", instance -> instance::isDead);
    public static final EventTemplateV1 E_SLEEPING = new EventTemplateV1("sleeping", instance -> instance::isSleeping);
    public static final EventTemplateV1 E_FISHING = new EventTemplateV1("fishing", instance -> instance::isFishing);
    public static final EventTemplateV1 E_UNDER_WATER = new EventTemplateV1("under_water", instance -> instance::isUnderWater);
    public static final EventTemplateV1 E_IN_LAVA = new EventTemplateV1("in_lava", instance -> instance::inLava);

    // Mount events
    public static final EventTemplateV1 E_MINECART = new EventTemplateV1("minecart", instance -> instance::inMinecart);
    public static final EventTemplateV1 E_BOAT = new EventTemplateV1("boat", instance -> instance::inBoat);
    public static final EventTemplateV1 E_HORSE = new EventTemplateV1("horse", instance -> instance::onHorse);
    public static final EventTemplateV1 E_DONKEY = new EventTemplateV1("donkey", instance -> instance::onDonkey);
    public static final EventTemplateV1 E_PIG = new EventTemplateV1("pig", instance -> instance::onPig);
    public static final EventTemplateV1 E_ELYTRA = new EventTemplateV1("elytra", instance -> instance::flyingElytra);

    // Combat events
    public static final EventTemplateV1 E_IN_COMBAT = new EventTemplateV1("in_combat", instance -> instance::inCombat);
    public static final EventTemplateV1 E_BOSS_FIGHT = new EventTemplateV1("boss_fight", instance -> instance::inBossFight);

    public static final EventTemplateV1[] EVENTS = new EventTemplateV1[] {
            E_MAIN_MENU, E_JOINING, E_DISCONNECTED, E_CREDITS, E_PAUSED, E_IN_GAME,
            E_DAY, E_DAWN, E_DUSK, E_NIGHT,
            E_DOWNFALL, E_RAIN, E_SNOW, E_THUNDERING,
            E_VILLAGE, E_RANCH,
            E_DEAD, E_SLEEPING, E_FISHING, E_UNDER_WATER, E_IN_LAVA,
            E_MINECART, E_BOAT, E_HORSE, E_DONKEY, E_PIG, E_ELYTRA,
            E_IN_COMBAT, E_BOSS_FIGHT
    };


    // World properties
    public static final PropertyTemplateV1 P_DIMENSION = new PropertyTemplateV1("dimension", new StringT(), instance -> instance::getDimensionId);
    public static final PropertyTemplateV1 P_BIOME = new PropertyTemplateV1("biome", new StringT(), instance -> instance::getBiomeId);
    public static final PropertyTemplateV1 P_BIOME_TAGS = new PropertyTemplateV1("biome_tags", new ListT(new StringT()), instance -> instance::getBiomeTagIDs);
    public static final PropertyTemplateV1 P_TIME = new PropertyTemplateV1("time", new IntT(), instance -> instance::getTime);
    public static final PropertyTemplateV1 P_CAVE_SCORE = new PropertyTemplateV1("cave_score", new FloatT(), instance -> instance::getCaveScore);

    // Player properties
    public static final PropertyTemplateV1 P_HEALTH = new PropertyTemplateV1("health", new FloatT(), instance -> instance::getPlayerHealth);
    public static final PropertyTemplateV1 P_MAX_HEALTH = new PropertyTemplateV1("max_health", new FloatT(), instance -> instance::getPlayerMaxHealth);
    public static final PropertyTemplateV1 P_ELEVATION = new PropertyTemplateV1("elevation", new FloatT(), instance -> instance::getPlayerElevation);
    public static final PropertyTemplateV1 P_VEHICLE = new PropertyTemplateV1("vehicle", new StringT(), instance -> instance::getVehicleId);
    public static final PropertyTemplateV1 P_EFFECTS = new PropertyTemplateV1("effects", new ListT(new StringT()), instance -> instance::getActiveEffects);

    // Combat properties
    public static final PropertyTemplateV1 P_COMBATANT_COUNT = new PropertyTemplateV1("combatant_count", new IntT(), instance -> instance::countCombatants);
    public static final PropertyTemplateV1 P_BOSS = new PropertyTemplateV1("boss", new StringT(), instance -> instance::getBoss);
    public static final PropertyTemplateV1 P_BOSSES = new PropertyTemplateV1("bosses", new ListT(new StringT()), instance -> instance::getBosses);

    public static final PropertyTemplateV1[] PROPERTIES = new PropertyTemplateV1[] {
            P_DIMENSION, P_BIOME, P_BIOME_TAGS, P_TIME, P_CAVE_SCORE,
            P_HEALTH, P_MAX_HEALTH, P_ELEVATION, P_VEHICLE, P_EFFECTS,
            P_COMBATANT_COUNT, P_BOSS, P_BOSSES
    };


    public GameStateProviderV1Template()
    {
        for (var event : EVENTS)
            registerEvent(event.name, event.getter.apply(this));

        for (var property : PROPERTIES)
            registerProperty(property.name, property.type, property.getter.apply(this));
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
    public abstract BoolVal inCombat();
    public abstract BoolVal inBossFight();



    // ------------------------------------------------------------------------------------------------
    // World properties
    public abstract StringVal getDimensionId();
    public abstract StringVal getBiomeId();
    public abstract ListVal getBiomeTagIDs();
    public abstract IntVal getTime();
    public abstract FloatVal getCaveScore();


    // ------------------------------------------------------------------------------------------------
    // Player properties
    public abstract FloatVal getPlayerHealth();
    public abstract FloatVal getPlayerMaxHealth();
    public abstract FloatVal getPlayerElevation();
    public abstract StringVal getVehicleId();
    public abstract ListVal getActiveEffects();


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    public abstract IntVal countCombatants();
    public abstract StringVal getBoss();
    public abstract ListVal getBosses();


    public record EventTemplateV1(
            String name,
            Function<GameStateProviderV1Template, Supplier<BoolVal>> getter
    ) {}

    public record PropertyTemplateV1(
            String name,
            Type type,
            Function<GameStateProviderV1Template, Supplier<Value>> getter
    ) {}
}
