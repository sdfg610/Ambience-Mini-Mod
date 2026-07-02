package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers.ValueList;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers.ValueMap;
import me.molybdenum.ambience_mini.engine.client.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.client.core.providers.GameStateProviderTemplate;
import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.McVersion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

public class GameStateProviderMock extends GameStateProviderTemplate
{
    public HashMap<String, BoolVal> eventValues = new HashMap<>();
    public HashMap<String, String> propertyValues = new HashMap<>() {{
        put(P_DIFFICULTY.name(), "peaceful");
        put(P_DIMENSION.name(), "minecraft:overworld");
        put(P_BIOME.name(), "minecraft:forest");
        put(P_BIOME_TAGS.name(), "minecraft:is_beach, minecraft:is_ocean");
        put(P_TIME.name(), "0");
        put(P_CAVE_SCORE.name(), "0.0");
        put(P_SKYLIGHT_SCORE.name(), "0.0");

        put(P_AREAS.name(), "{ name=\"Home\", dimension=\"minecraft:overworld\", owner=\"some_uuid\", isShared=true, isLocal=false } ,\n { name=\"Safe spot\", dimension=\"minecraft:the_end\", owner=undefined, isShared=false, isLocal=true }\n");
        put(P_STRUCTURES.name(), "minecraft:village_plains");

        put(P_UUID.name(), "some_uuid");
        put(P_GAME_MODE.name(), "survival");
        put(P_HEALTH.name(), "20.0");
        put(P_MAX_HEALTH.name(), "20.0");
        put(P_ELEVATION.name(), "80.0");
        put(P_VEHICLE.name(), "minecraft:minecart");
        put(P_EFFECTS.name(), "minecraft:absorption, minecraft:regeneration");

        put(P_COMBATANT_COUNT.name(), "0");
        put(P_COMBATANTS.name(), "{ type_id=\"minecraft:skeleton\", health=10.0, max_health=20.0, targeting_player=true, fighting_player=true } ,\n { type_id=\"minecraft:creeper\", health=7.0, max_health=10.0, targeting_player=true, fighting_player=false }");
        put(P_BOSS.name(), "entity.minecraft.ender_dragon");
        put(P_BOSSES.name(), "entity.minecraft.ender_dragon, entity.minecraft.wither");

        put(P_FLAGS.name(), "{ \"flag_1\": \"value 1\", \"flag_2\": \"value 2\" }");
    }};


    public GameStateProviderMock() {
        super(McVersion.ANY);
        for (var event : EVENTS)
            eventValues.put(event.name(), BoolVal.FALSE);
    }


    public void setEventValue(String ev, BoolVal val) {
        eventValues.put(ev, val);
    }


    public String getPropertyValueString(String prop) {
        return propertyValues.get(prop);
    }

    public void setPropertyValueString(String prop, String val) {
        propertyValues.put(prop, val);
    }


    // ------------------------------------------------------------------------------------------------
    // Parsing of values
    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        assert messages != null;
        for (var property : PROPERTIES)
            if (!isValidPropertyValue(property))
                messages.add("Value given for property '$" + property.name() + "' is not a valid value of type '" + PrettyPrinter.getTypeString(property.type()) + "'!");
    }

    private boolean isValidPropertyValue(PropertyTemplateV1 property) {
        var value = propertyValues.get(property.name());
        if (property.type() instanceof IntT)
            return isValidIntVal(value);
        else if (property.type() instanceof FloatT)
            return isValidFloatVal(value);
        else if (property.type() instanceof StringT)
            return true;
        else if (property.type() instanceof ListT lst) {
            if (lst.elementType instanceof StringT)
                return true;
            else if (lst.elementType instanceof AreaT)
                return isValidAreaListVal(value);
            else if (lst.elementType instanceof CombatantT)
                return isValidCombatantListVal(value);
        }
        else if (property.type() instanceof MapT map) {
            if (map.keyType instanceof StringT && map.valueType instanceof StringT)
                return isValidStringStringMapVal(value);
        }

        throw new RuntimeException("Unhandled type in check for valid values! Type: '" + PrettyPrinter.getTypeString(property.type()) + "'");
    }

    
    // Int
    private boolean isValidIntVal(String value) {
        var trim = value.trim();
        return trim.matches("^[0-9]+$") || trim.equalsIgnoreCase("undefined");
    }

    private IntVal getAsIntVal(PropertyTemplateV1 property) {
        try {
            return new IntVal(Integer.parseInt(propertyValues.get(property.name()).trim()));
        } catch (NumberFormatException e) {
            return IntVal.UNDEFINED;
        }
    }

    // Float
    private boolean isValidFloatVal(String value) {
        var trim = value.trim();
        return trim.matches("[0-9]+(\\.[0-9]*)?") || trim.equalsIgnoreCase("undefined");
    }

    private FloatVal getAsFloatVal(PropertyTemplateV1 property) {
        try {
            return new FloatVal(Float.parseFloat(propertyValues.get(property.name()).trim()));
        } catch (NumberFormatException e) {
            return FloatVal.UNDEFINED;
        }
    }

    // String
    private StringVal getAsStringVal(PropertyTemplateV1 property) {
        return new StringVal(propertyValues.get(property.name()));
    }

    // String List
    private ListVal getAsStringListVal(PropertyTemplateV1 property) {
        return ListVal.ofStringList(
                Arrays.stream(propertyValues.get(property.name()).split(",")).map(String::trim).toList()
        );
    }

    // Area List
    private static final Pattern fieldPattern = Pattern.compile(
            "([_a-zA-Z][_a-zA-Z0-9]*)\\s*=\\s*((?i)\"[^\\n\\r\"]*\"|[0-9]+|[0-9]+\\.[0-9]+|true|false|undefined)"
    );
    private static final Pattern recordPattern = Pattern.compile(
            "\\{\\s*(" + fieldPattern.pattern() + ")?(?:\\s*,\\s*(" + fieldPattern.pattern() + "))*\\s*}"
    );
    private static final Pattern recordListPattern = Pattern.compile(
            "(" + recordPattern.pattern() + "\\s*(?:\\s*,\\s*" + recordPattern.pattern() + ")*)?"
    );

    private boolean isValidAreaListVal(String value) {
        var trim = value.trim();
        return recordListPattern.matcher(trim).matches()
                && getAsAreaListVal(trim) != null;
    }

    private ListVal getAsAreaListVal(PropertyTemplateV1 property) {
        return getAsAreaListVal(propertyValues.get(property.name()));
    }

    private ListVal getAsAreaListVal(String value) {
        var values = new ValueList();
        var areaMatcher = recordPattern.matcher(value);
        while (areaMatcher.find()) {
            var fieldMatcher = fieldPattern.matcher(areaMatcher.group());

            String name = null;
            String dimension = null;
            String owner = null;
            Boolean isShared = null;
            Boolean isLocal = null;

            while (fieldMatcher.find()) {
                var nameAndValue = fieldMatcher.group().split("=");
                switch (nameAndValue[0]) {
                    case "name" -> name = fieldAsString(nameAndValue[1]);
                    case "dimension" -> dimension = fieldAsString(nameAndValue[1]);
                    case "owner" -> owner = fieldAsString(nameAndValue[1]);
                    case "isShared" -> isShared = fieldAsBoolean(nameAndValue[1]);
                    case "isLocal" -> isLocal = fieldAsBoolean(nameAndValue[1]);
                    default -> {
                        return null;
                    }
                }
            }

            values.add(new AreaVal(new AreaVal.AreaDescriptor(name, dimension, owner, isShared, isLocal)));
        }

        return new ListVal(values);
    }

    private String fieldAsString(String val) {
        if (val.startsWith("\""))
            return val.substring(1, val.length()-1);
        return null;
    }

    private Float fieldAsFloat(String val) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Boolean fieldAsBoolean(String val) {
        return switch (val.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };
    }

    // String String Map
    private static final Pattern elementPattern = Pattern.compile(
            "\"([^\"\\n\\r]*)\"\\s*:\\s*\"([^\"\\n\\r]*)\""
    );

    private static final Pattern strStrMapPattern = Pattern.compile(
            "(\\{\\s*(" + elementPattern + "\\s*(,\\s*" + elementPattern + "\\s*)*)?})?"
    );

    private boolean isValidStringStringMapVal(String value) {
        var trim = value.trim();
        if (!strStrMapPattern.matcher(trim).matches())
            return false;

        var elementMatcher = elementPattern.matcher(value);
        while (elementMatcher.find())
            if (!FlagManager.validateId(elementMatcher.group(1)) || !FlagManager.validateValue(elementMatcher.group(2)))
                return false;

        return true;
    }

    private MapVal getAsStringStringMapVal(PropertyTemplateV1 property) {
        var map = new ValueMap();
        var elementMatcher = elementPattern.matcher(propertyValues.get(property.name()));
        while (elementMatcher.find()) {
            String key = elementMatcher.group(1);
            String val = elementMatcher.group(2);
            map.put(new StringVal(key), new StringVal(val));
        }
        return new MapVal(map);
    }


    // Combatant List
    private boolean isValidCombatantListVal(String value) {
        var trim = value.trim();
        return
                recordListPattern.matcher(trim).matches()
                        && getAsCombatantListVal(trim) != null;
    }

    private ListVal getAsCombatantListVal(PropertyTemplateV1 property) {
        return getAsCombatantListVal(propertyValues.get(property.name()));
    }

    private ListVal getAsCombatantListVal(String value) {
        var values = new ValueList();
        var areaMatcher = recordPattern.matcher(value);
        while (areaMatcher.find()) {
            var fieldMatcher = fieldPattern.matcher(areaMatcher.group());

            String type_id = null;
            Float health = null;
            Float max_health = null;
            Boolean isTargetingPlayer = null;
            boolean isFightingPlayer = false;

            while (fieldMatcher.find()) {
                var nameAndValue = fieldMatcher.group().split("=");
                switch (nameAndValue[0]) {
                    case CombatantT.FIELD_TYPE_ID -> type_id = fieldAsString(nameAndValue[1]);
                    case CombatantT.FIELD_HEALTH -> health = fieldAsFloat(nameAndValue[1]);
                    case CombatantT.FIELD_MAX_HEALTH -> max_health = fieldAsFloat(nameAndValue[1]);
                    case CombatantT.FIELD_TARGETING_PLAYER -> isTargetingPlayer = fieldAsBoolean(nameAndValue[1]);
                    case CombatantT.FIELD_FIGHTING_PLAYER -> isFightingPlayer = Boolean.TRUE.equals(fieldAsBoolean(nameAndValue[1]));
                    default -> {
                        return null;
                    }
                }
            }

            values.add(new CombatantVal(new CombatantVal.CombatantDescriptor(type_id, health, max_health, isTargetingPlayer, isFightingPlayer)));
        }

        return new ListVal(values);
    }



    // ------------------------------------------------------------------------------------------------
    // Global events
    @Override
    public BoolVal inMainMenu() {
        return eventValues.get(E_MAIN_MENU.name());
    }

    @Override
    public BoolVal isJoiningWorld() {
        return eventValues.get(E_JOINING.name());
    }

    @Override
    public BoolVal isDisconnected() {
        return eventValues.get(E_DISCONNECTED.name());
    }

    @Override
    public BoolVal onCreditsScreen() {
        return eventValues.get(E_CREDITS.name());
    }

    @Override
    public BoolVal isPaused() {
        return eventValues.get(E_PAUSED.name());
    }

    @Override
    public BoolVal inGame() {
        return eventValues.get(E_IN_GAME.name());
    }


    // ------------------------------------------------------------------------------------------------
    // Time events
    @Override
    public BoolVal isDay() {
        return eventValues.get(E_DAY.name());
    }

    @Override
    public BoolVal isDawn() {
        return eventValues.get(E_DAWN.name());
    }

    @Override
    public BoolVal isDusk() {
        return eventValues.get(E_DUSK.name());
    }

    @Override
    public BoolVal isNight() {
        return eventValues.get(E_NIGHT.name());
    }


    // ------------------------------------------------------------------------------------------------
    // Weather events
    @Override
    public BoolVal isDownfall() {
        return eventValues.get(E_DOWNFALL.name());
    }

    @Override
    public BoolVal isRaining() {
        return eventValues.get(E_RAIN.name());
    }

    @Override
    public BoolVal isSnowing() {
        return eventValues.get(E_SNOW.name());
    }

    @Override
    public BoolVal isThundering() {
        return eventValues.get(E_THUNDERING.name());
    }


    // ------------------------------------------------------------------------------------------------
    // Location events
    @Override
    public BoolVal inVillage() {
        return eventValues.get(E_VILLAGE.name());
    }

    @Override
    public BoolVal inRanch() {
        return eventValues.get(E_RANCH.name());
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    @Override
    public BoolVal isDead() {
        return eventValues.get(E_DEAD.name());
    }

    @Override
    public BoolVal isSleeping() {
        return eventValues.get(E_SLEEPING.name());
    }

    @Override
    public BoolVal isFishing() {
        return eventValues.get(E_FISHING.name());
    }

    @Override
    public BoolVal isUnderWater() {
        return eventValues.get(E_UNDER_WATER.name());
    }

    @Override
    public BoolVal inLava() {
        return eventValues.get(E_IN_LAVA.name());
    }

    @Override
    public BoolVal onFire()  {
        return eventValues.get(E_ON_FIRE.name());
    }

    @Override
    public BoolVal inPowderSnow()  {
        return eventValues.get(E_IN_POWDER_SNOW.name());
    }

    @Override
    public BoolVal isDrowning() {
        return eventValues.get(E_DROWNING.name());
    }


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    @Override
    public BoolVal inMinecart() {
        return eventValues.get(E_MINECART.name());
    }

    @Override
    public BoolVal inBoat() {
        return eventValues.get(E_BOAT.name());
    }

    @Override
    public BoolVal onHorse() {
        return eventValues.get(E_HORSE.name());
    }

    @Override
    public BoolVal onDonkey() {
        return eventValues.get(E_DONKEY.name());
    }

    @Override
    public BoolVal onPig() {
        return eventValues.get(E_PIG.name());
    }

    @Override
    public BoolVal flyingElytra() {
        return eventValues.get(E_ELYTRA.name());
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    @Override
    public BoolVal wardenNearby() {
        return eventValues.get(E_WARDEN_NEARBY.name());
    }

    @Override
    public BoolVal isTargeted() {
        return eventValues.get(E_IS_TARGETED.name());
    }

    @Override
    public BoolVal inCombat() {
        return eventValues.get(E_IN_COMBAT.name());
    }

    @Override
    public BoolVal inBossFight() {
        return eventValues.get(E_BOSS_FIGHT.name());
    }



    // ------------------------------------------------------------------------------------------------
    // World properties
    @Override
    public StringVal getDifficulty() {
        return getAsStringVal(P_DIFFICULTY);
    }

    @Override
    public StringVal getDimensionId() {
        return getAsStringVal(P_DIMENSION);
    }

    @Override
    public StringVal getBiomeId() {
        return getAsStringVal(P_BIOME);
    }

    @Override
    public ListVal getBiomeTagIDs() {
        return getAsStringListVal(P_BIOME_TAGS);
    }

    @Override
    public IntVal getTime() {
        return getAsIntVal(P_TIME);
    }

    @Override
    public FloatVal getCaveScore() {
        return getAsFloatVal(P_CAVE_SCORE);
    }

    @Override
    public FloatVal getSkylightScore() {
        return getAsFloatVal(P_SKYLIGHT_SCORE);
    }


    // ------------------------------------------------------------------------------------------------
    // Location properties
    @Override
    public ListVal getIntersectingAreas() {
        return getAsAreaListVal(P_AREAS);
    }

    @Override
    public ListVal getIntersectingStructures() {
        return getAsStringListVal(P_STRUCTURES);
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    @Override
    public StringVal getPlayerUUID() {
        return getAsStringVal(P_UUID);
    }

    @Override
    public StringVal getGameMode() {
        return getAsStringVal(P_GAME_MODE);
    }

    @Override
    public FloatVal getPlayerHealth() {
        return getAsFloatVal(P_HEALTH);
    }

    @Override
    public FloatVal getPlayerMaxHealth() {
        return getAsFloatVal(P_MAX_HEALTH);
    }

    @Override
    public FloatVal getPlayerElevation() {
        return getAsFloatVal(P_ELEVATION);
    }

    @Override
    public StringVal getVehicleId() {
        return getAsStringVal(P_VEHICLE);
    }

    @Override
    public ListVal getActiveEffects() {
        return getAsStringListVal(P_EFFECTS);
    }


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    @Override
    public IntVal countCombatants() {
        return getAsIntVal(P_COMBATANT_COUNT);
    }

    @Override
    public ListVal getCombatants() {
        return getAsCombatantListVal(P_COMBATANTS);
    }

    @Override
    public StringVal getBoss() {
        return getAsStringVal(P_BOSS);
    }

    @Override
    public ListVal getBosses() {
        return getAsStringListVal(P_BOSSES);
    }

    @Override
    public MapVal getFlags() {
        return getAsStringStringMapVal(P_FLAGS);
    }
}
