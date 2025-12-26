package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameStateProviderV1Mock extends GameStateProviderV1Template
{
    public HashMap<String, Boolean> eventValues = new HashMap<>();
    public HashMap<String, Object> propertyValues = new HashMap<>() {{
        put(P_DIFFICULTY.name(), "peaceful");
        put(P_DIMENSION.name(), "minecraft:overworld");
        put(P_BIOME.name(), "minecraft:forest");
        put(P_BIOME_TAGS.name(), List.of("minecraft:is_beach", "minecraft:is_ocean"));
        put(P_TIME.name(), 0);
        put(P_CAVE_SCORE.name(), 0.0f);
        put(P_SKYLIGHT_SCORE.name(), 0.0f);

        put(P_GAME_MODE.name(), "survival");
        put(P_HEALTH.name(), 20f);
        put(P_MAX_HEALTH.name(), 20f);
        put(P_ELEVATION.name(), 80f);
        put(P_VEHICLE.name(), "minecraft:minecart");
        put(P_EFFECTS.name(), List.of("minecraft:absorption", "minecraft:regeneration"));

        put(P_COMBATANT_COUNT.name(), 0);
        put(P_BOSS.name(), "entity.minecraft.ender_dragon");
        put(P_BOSSES.name(), List.of("entity.minecraft.ender_dragon", "entity.minecraft.wither"));
    }};


    public GameStateProviderV1Mock() {
        for (var event : EVENTS)
            eventValues.put(event.name(), false);
    }


    public <T> T getPropertyValue(String property) {
        //noinspection unchecked
        return (T)propertyValues.get(property);
    }


    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        // Check for errors in custom event/property values? Malformed number, string, or list?
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    @Override
    public BoolVal inMainMenu() {
        return new BoolVal(eventValues.get(E_MAIN_MENU.name()));
    }

    @Override
    public BoolVal isJoiningWorld() {
        return new BoolVal(eventValues.get(E_JOINING.name()));
    }

    @Override
    public BoolVal isDisconnected() {
        return new BoolVal(eventValues.get(E_DISCONNECTED.name()));
    }

    @Override
    public BoolVal onCreditsScreen() {
        return new BoolVal(eventValues.get(E_CREDITS.name()));
    }

    @Override
    public BoolVal isPaused() {
        return new BoolVal(eventValues.get(E_PAUSED.name()));
    }

    @Override
    public BoolVal inGame() {
        return new BoolVal(eventValues.get(E_IN_GAME.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Time events
    @Override
    public BoolVal isDay() {
        return new BoolVal(eventValues.get(E_DAY.name()));
    }

    @Override
    public BoolVal isDawn() {
        return new BoolVal(eventValues.get(E_DAWN.name()));
    }

    @Override
    public BoolVal isDusk() {
        return new BoolVal(eventValues.get(E_DUSK.name()));
    }

    @Override
    public BoolVal isNight() {
        return new BoolVal(eventValues.get(E_NIGHT.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Weather events
    @Override
    public BoolVal isDownfall() {
        return new BoolVal(eventValues.get(E_DOWNFALL.name()));
    }

    @Override
    public BoolVal isRaining() {
        return new BoolVal(eventValues.get(E_RAIN.name()));
    }

    @Override
    public BoolVal isSnowing() {
        return new BoolVal(eventValues.get(E_SNOW.name()));
    }

    @Override
    public BoolVal isThundering() {
        return new BoolVal(eventValues.get(E_THUNDERING.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Location events
    @Override
    public BoolVal inVillage() {
        return new BoolVal(eventValues.get(E_VILLAGE.name()));
    }

    @Override
    public BoolVal inRanch() {
        return new BoolVal(eventValues.get(E_RANCH.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    @Override
    public BoolVal isDead() {
        return new BoolVal(eventValues.get(E_DEAD.name()));
    }

    @Override
    public BoolVal isSleeping() {
        return new BoolVal(eventValues.get(E_SLEEPING.name()));
    }

    @Override
    public BoolVal isFishing() {
        return new BoolVal(eventValues.get(E_FISHING.name()));
    }

    @Override
    public BoolVal isUnderWater() {
        return new BoolVal(eventValues.get(E_UNDER_WATER.name()));
    }

    @Override
    public BoolVal inLava() {
        return new BoolVal(eventValues.get(E_IN_LAVA.name()));
    }

    @Override
    public BoolVal isDrowning() {
        return new BoolVal(eventValues.get(E_DROWNING.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    @Override
    public BoolVal inMinecart() {
        return new BoolVal(eventValues.get(E_MINECART.name()));
    }

    @Override
    public BoolVal inBoat() {
        return new BoolVal(eventValues.get(E_BOAT.name()));
    }

    @Override
    public BoolVal onHorse() {
        return new BoolVal(eventValues.get(E_HORSE.name()));
    }

    @Override
    public BoolVal onDonkey() {
        return new BoolVal(eventValues.get(E_DONKEY.name()));
    }

    @Override
    public BoolVal onPig() {
        return new BoolVal(eventValues.get(E_PIG.name()));
    }

    @Override
    public BoolVal flyingElytra() {
        return new BoolVal(eventValues.get(E_ELYTRA.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    @Override
    public BoolVal inCombat() {
        return new BoolVal(eventValues.get(E_IN_COMBAT.name()));
    }

    @Override
    public BoolVal inBossFight() {
        return new BoolVal(eventValues.get(E_BOSS_FIGHT.name()));
    }



    // ------------------------------------------------------------------------------------------------
    // World properties
    @Override
    public StringVal getDifficulty() {
        return new StringVal(getPropertyValue(P_DIFFICULTY.name()));
    }

    @Override
    public StringVal getDimensionId() {
        return new StringVal(getPropertyValue(P_DIMENSION.name()));
    }

    @Override
    public StringVal getBiomeId() {
        return new StringVal(getPropertyValue(P_BIOME.name()));
    }

    @Override
    public ListVal getBiomeTagIDs() {
        return ListVal.ofStringList(getPropertyValue(P_BIOME_TAGS.name()));
    }

    @Override
    public IntVal getTime() {
        return new IntVal(getPropertyValue(P_TIME.name()));
    }

    @Override
    public FloatVal getCaveScore() {
        return new FloatVal(getPropertyValue(P_CAVE_SCORE.name()));
    }

    @Override
    public FloatVal getSkylightScore() {
        return new FloatVal(getPropertyValue(P_SKYLIGHT_SCORE.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    @Override
    public StringVal getGameMode() {
        return new StringVal(getPropertyValue(P_GAME_MODE.name()));
    }

    @Override
    public FloatVal getPlayerHealth() {
        return new FloatVal(getPropertyValue(P_HEALTH.name()));
    }

    @Override
    public FloatVal getPlayerMaxHealth() {
        return new FloatVal(getPropertyValue(P_MAX_HEALTH.name()));
    }

    @Override
    public FloatVal getPlayerElevation() {
        return new FloatVal(getPropertyValue(P_ELEVATION.name()));
    }

    @Override
    public StringVal getVehicleId() {
        return new StringVal(getPropertyValue(P_VEHICLE.name()));
    }

    @Override
    public ListVal getActiveEffects() {
        return ListVal.ofStringList(getPropertyValue(P_EFFECTS.name()));
    }


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    @Override
    public IntVal countCombatants() {
        return new IntVal(getPropertyValue(P_COMBATANT_COUNT.name()));
    }

    @Override
    public StringVal getBoss() {
        return new StringVal(getPropertyValue(P_BOSS.name()));
    }

    @Override
    public ListVal getBosses() {
        return ListVal.ofStringList(getPropertyValue(P_BOSSES.name()));
    }
}
