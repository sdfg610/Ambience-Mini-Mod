package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_ide.parsers.*;
import me.molybdenum.ambience_mini.engine.server.core.music.ServerConfigInterpreter;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.helpers.ValueMap;
import me.molybdenum.ambience_mini.engine.shared.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.client.core.providers.GameStateProviderTemplate;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.McVersion;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class GameStateProviderMock extends GameStateProviderTemplate
{
    public HashMap<String, BoolVal> eventValues = new HashMap<>();
    public HashMap<String, String> propertyValues = new HashMap<>() {{
        put(P_SCREEN_ID.name(), "net.minecraft.client.gui.screens.TitleScreen");
        put(P_MENU.name(), "main_menu");

        put(P_DIFFICULTY.name(), "peaceful");
        put(P_DIMENSION.name(), "minecraft:overworld");
        put(P_BIOME.name(), "minecraft:forest");
        put(P_BIOME_TAGS.name(), "[ \"minecraft:is_beach\", \"minecraft:is_ocean\" ]");
        put(P_TIME.name(), "0");
        put(P_CAVE_SCORE.name(), "0.0");
        put(P_SKYLIGHT_SCORE.name(), "0.0");

        put(P_AREAS.name(), "[ { name=\"Home\", dimension=\"minecraft:overworld\", owner=\"some_uuid\", isShared=true, isLocal=false } ,\n { name=\"Safe spot\", dimension=\"minecraft:the_end\", owner=undefined, isShared=false, isLocal=true } ]\n");
        put(P_STRUCTURES.name(), "[ \"minecraft:village_plains\" ]");

        put(P_UUID.name(), "some_uuid");
        put(P_GAME_MODE.name(), "survival");
        put(P_HEALTH.name(), "20.0");
        put(P_MAX_HEALTH.name(), "20.0");
        put(P_ELEVATION.name(), "80.0");
        put(P_VEHICLE.name(), "minecraft:minecart");
        put(P_EFFECTS.name(), "[ \"minecraft:absorption\", \"minecraft:regeneration\" ]");

        put(P_COMBATANT_COUNT.name(), "0");
        put(P_COMBATANTS.name(), "[ { type_id=\"minecraft:skeleton\", health=10.0, max_health=20.0, targeting_player=true, fighting_player=true } ,\n { type_id=\"minecraft:creeper\", health=7.0, max_health=10.0, targeting_player=true, fighting_player=false } ]\n");
        put(P_BOSS.name(), "entity.minecraft.ender_dragon");
        put(P_BOSSES.name(), "[ \"entity.minecraft.ender_dragon\", \"entity.minecraft.wither\" ]");
        put(P_BOSS_INFOS.name(), "[ { id=\"entity.minecraft.ender_dragon\", health_percent=100.0 } ]");

        put(P_LIVING.name(), "[ { type_id=\"minecraft:bat\", health=6.0, max_health=6.0 }, { type_id=\"minecraft:sheep\", health=8.0, max_health=8.0 } ]");

        put(P_FLAGS.name(), "{ \"flag_1\": \"value 1\", \"flag_2\": \"value 2\" }");
    }};

    private MapVal serverPlaylists = MapVal.UNDEFINED;


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


    public void setPlaylists(ServerConfigInterpreter serverConfigInterpreter) {
        ValueMap map = new ValueMap();
        if (serverConfigInterpreter.hasServerPlaylists()) {
            try {
                for (var playlistDTO : serverConfigInterpreter.loadServerPlaylists()) {
                    var list = playlistDTO.music().stream()
                            .map(music -> music.toMusic(true))
                            .toList();
                    map.put(
                            new StringVal(playlistDTO.key()),
                            new PlaylistVal(list)
                    );
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            serverPlaylists = new MapVal(map);
        }
        else
            serverPlaylists = MapVal.UNDEFINED;
    }


    private String get(PropertyTemplate prop) {
        return propertyValues.get(prop.name()).trim();
    }


    // ------------------------------------------------------------------------------------------------
    // Validation
    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        assert messages != null;
        for (var property : PROPERTIES) {
            validateProperty(property).ifPresent(msg ->
                    messages.add("Invalid value for '$" + property.name() + "': " + msg)
            );
        }
    }

    private Optional<String> validateProperty(PropertyTemplate property) {
        var value = propertyValues.get(property.name());
        if (property.type() instanceof IntT)
            return IntParser.validate(value);
        else if (property.type() instanceof FloatT)
            return FloatParser.validate(value);
        else if (property.type() instanceof StringT)
            return Optional.empty();
        else if (property.type() instanceof ListT lst) {
            if (value == null || value.isBlank())
                return Optional.empty();
            else if (lst.elementType instanceof StringT)
                return ListParser.validateStringList(value);
            else if (lst.elementType instanceof AreaT)
                return ListParser.validateAreaList(value);
            else if (lst.elementType instanceof CombatantT)
                return ListParser.validateCombatantList(value);
            else if (lst.elementType instanceof BossT)
                return ListParser.validateBossList(value);
            else if (lst.elementType instanceof LivingT)
                return ListParser.validateLivingList(value);
        }
        else if (property.type() instanceof MapT map) {
            if (map.keyType instanceof StringT && map.valueType instanceof StringT)
                return MapParser.validateFlagMap(value);
            if (map.keyType instanceof StringT && map.valueType instanceof PlaylistT)
                return Optional.empty();
        }

        throw new RuntimeException("Unhandled type in value validation! Type: '" + PrettyPrinter.getTypeString(property.type()) + "'");
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
    public BoolVal isFighting() {
        return eventValues.get(E_IS_FIGHTING.name());
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
    // Global properties
    @Override
    public StringVal getMenuID() {
        return StringParser.parseRaw(get(P_SCREEN_ID));
    }

    @Override
    public StringVal getMenuType() {
        return StringParser.parseRaw(get(P_MENU));  // TODO: Dropdown??????
    }


    // ------------------------------------------------------------------------------------------------
    // World properties
    public StringVal getMenuID() {
        return null; // return getAsStringVal(P_SCREEN_ID);
    }

    @Override
    public StringVal getMenuType() {
        return null; // return getAsStringVal(P_MENU);
    }

    @Override
    public StringVal getDifficulty() {
        return StringParser.parseRaw(get(P_DIFFICULTY));  // TODO: Dropdown??????
    }

    @Override
    public StringVal getDimensionId() {
        return StringParser.parseRaw(get(P_DIMENSION));
    }

    @Override
    public StringVal getBiomeId() {
        return StringParser.parseRaw(get(P_BIOME));
    }

    @Override
    public ListVal getBiomeTagIDs() {
        return ListParser.parseStringList(get(P_BIOME_TAGS));
    }

    @Override
    public IntVal getTime() {
        return IntParser.parse(get(P_TIME));
    }

    @Override
    public FloatVal getCaveScore() {
        return FloatParser.parse(get(P_CAVE_SCORE)); // TODO: Slider??????
    }

    @Override
    public FloatVal getSkylightScore() {
        return FloatParser.parse(get(P_SKYLIGHT_SCORE)); // TODO: Slider??????
    }


    // ------------------------------------------------------------------------------------------------
    // Location properties
    @Override
    public ListVal getIntersectingAreas() {
        return ListParser.parseAreaList(get(P_AREAS));
    }

    @Override
    public ListVal getIntersectingStructures() {
        return ListParser.parseStringList(get(P_STRUCTURES));
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    @Override
    public StringVal getPlayerUUID() {
        return StringParser.parseRaw(get(P_UUID));
    }

    @Override
    public StringVal getGameMode() {
        return StringParser.parseRaw(get(P_GAME_MODE)); // TODO: Dropdown??????
    }

    @Override
    public FloatVal getPlayerHealth() {
        return FloatParser.parse(get(P_HEALTH));
    }

    @Override
    public FloatVal getPlayerMaxHealth() {
        return FloatParser.parse(get(P_MAX_HEALTH));
    }

    @Override
    public FloatVal getPlayerElevation() {
        return FloatParser.parse(get(P_ELEVATION));
    }

    @Override
    public StringVal getVehicleId() {
        return StringParser.parseRaw(get(P_VEHICLE));
    }

    @Override
    public ListVal getActiveEffects() {
        return ListParser.parseStringList(get(P_EFFECTS));
    }


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    @Override
    public IntVal countCombatants() {
        return IntParser.parse(get(P_COMBATANT_COUNT));
    }

    @Override
    public ListVal getCombatants() {
        return ListParser.parseCombatantList(get(P_COMBATANTS));
    }

    @Override
    public StringVal getBoss() {
        return StringParser.parseRaw(get(P_BOSS));
    }

    @Override
    public ListVal getBosses() {
        return ListParser.parseStringList(get(P_BOSSES));
    }

    @Override
    public ListVal getBossInfos() {
        return ListParser.parseBossList(get(P_BOSS_INFOS));
    }


    // ------------------------------------------------------------------------------------------------
    // Entity properties
    @Override
    public Value<?> getNearbyLiving() {
        return ListParser.parseLivingList(get(P_LIVING));
    }


    // ------------------------------------------------------------------------------------------------
    // Flag properties
    @Override
    public MapVal getFlags() {
        return MapParser.parseStringStringMap(get(P_FLAGS));
    }


    // ------------------------------------------------------------------------------------------------
    // Server playlists properties
    @Override
    public Value<?> getServerPlaylists() {
        return serverPlaylists;
    }
}
