package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.FloatT;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.IntT;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.ListT;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.StringT;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.core.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.core.state.BaseCombatState;
import me.molybdenum.ambience_mini.engine.core.state.Screens;
import me.molybdenum.ambience_mini.engine.core.state.BaseLevelState;
import me.molybdenum.ambience_mini.engine.core.state.BasePlayerState;
import me.molybdenum.ambience_mini.engine.core.state.BaseScreenState;

public class GameStateProviderV1<TBlockPos, TVec3, TBlockState, TEntity> extends BaseGameStateProvider
{
    // State
    private final BasePlayerState<TBlockPos, TVec3> _player;
    private final BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity> _level;
    private final BaseScreenState _screen;
    private final BaseCombatState<TEntity, TVec3> _combat;
    private final CaveDetector<TBlockPos, TVec3, TBlockState> _caveDetector;

    // Config
    private final int _villageScanHorizontalRadius;
    private final int _villageScanVerticalRadius;
    private final int _villagerCountThreshold;

    private final int _ranchScanHorizontalRadius;
    private final int _ranchScanVerticalRadius;
    private final int _animalCountThreshold;

    private final int _fishingTimeout;
    private final int _fishingMoveThreshold;

    private final int _combatGracePeriod;

    // Cache
    private TVec3 _latestFishingPos = null;
    private long _latestFishingTime = 0L;
    private long _latestFishingHookInWaterTime = 0L;

    private long _latestCombatTime = 0L;

    private double _latestCaveScore = 0;

    // Properties used by other properties
    private Property timeProp;
    private Property bossesProp;


    public GameStateProviderV1(
        BaseClientConfig config,
        BasePlayerState<TBlockPos, TVec3> player,
        BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity> level,
        BaseScreenState screenMonitor,
        BaseCombatState<TEntity, TVec3> combatMonitor,
        CaveDetector<TBlockPos, TVec3, TBlockState> caveDetector
    ) {
        super();

        _player = player;
        _level = level;

        _screen = screenMonitor;
        _combat = combatMonitor;
        _caveDetector = caveDetector;

        _villageScanHorizontalRadius = config.villageScanHorizontalRadius.get();
        _villageScanVerticalRadius = config.villageScanVerticalRadius.get();
        _villagerCountThreshold = config.villagerCountThreshold.get();

        _ranchScanHorizontalRadius = config.ranchScanHorizontalRadius.get();
        _ranchScanVerticalRadius = config.ranchScanVerticalRadius.get();
        _animalCountThreshold = config.animalCountThreshold.get();

        _fishingTimeout = config.fishingTimeout.get();
        _fishingMoveThreshold = config.fishingMoveThreshold.get();

        _combatGracePeriod = config.combatGracePeriod.get();

        RegisterEventsAndProperties();
    }

    private void RegisterEventsAndProperties()
    {
        ////
        //// Events
        ////
        // Global events
        registerEvent("main_menu", this::inMainMenu);
        registerEvent("joining", this::isJoiningWorld);
        registerEvent("disconnected", this::isDisconnected);
        registerEvent("credits", this::onCreditsScreen);
        registerEvent("paused", this::isPaused);
        registerEvent("in_game", this::inGame);

        // Time events
        registerEvent("day", this::isDay);
        registerEvent("dawn", this::isDawn);
        registerEvent("dusk", this::isDusk);
        registerEvent("night", this::isNight);

        // Weather
        registerEvent("downfall", this::isDownfall);
        registerEvent("rain", this::isRaining);
        registerEvent("snow", this::isSnowing);
        registerEvent("thunder", this::isThundering);

        // Special locations
        registerEvent("village", this::inVillage);
        registerEvent("ranch", this::inRanch);

        // Player state
        registerEvent("dead", this::isDead);
        registerEvent("sleeping", this::isSleeping);
        registerEvent("fishing", this::isFishing);
        registerEvent("under_water", this::isUnderWater);
        registerEvent("in_lava", this::inLava);

        // Mounts
        registerEvent("minecart", this::inMinecart);
        registerEvent("boat", this::inBoat);
        registerEvent("horse", this::onHorse);
        registerEvent("donkey", this::onDonkey);
        registerEvent("pig", this::onPig);
        registerEvent("elytra", this::flyingElytra);

        // Combat
        registerEvent("in_combat", this::inCombat);
        registerEvent("boss_fight", this::inBossFight);


        ////
        //// Properties
        ////
        // World properties
        registerProperty("dimension", new StringT(), this::getDimensionId);
        registerProperty("biome", new StringT(), this::getBiomeId);
        registerProperty("biome_tags", new ListT(new StringT()), this::getBiomeTagIDs);
        timeProp = registerProperty("time", new IntT(), this::getTime);
        registerProperty("cave_score", new FloatT(), this::getCaveScore);

        // Player properties
        registerProperty("health", new FloatT(), this::getPlayerHealth);
        registerProperty("max_health", new FloatT(), this::getPlayerMaxHealth);
        registerProperty("elevation", new FloatT(), this::getPlayerElevation);
        registerProperty("vehicle", new StringT(), this::getVehicleId);
        registerProperty("effects", new ListT(new StringT()), this::getActiveEffects);

        // Combat properties
        registerProperty("combatant_count", new IntT(), this::countCombatants);
        registerProperty("boss", new StringT(), this::getBoss);
        bossesProp = registerProperty("bosses", new ListT(new StringT()), this::getBosses);


        //TODO: Structure detection maybe?
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    public BoolVal inMainMenu() {
        return new BoolVal(_screen.is(Screens.MAIN_MENU));
    }

    public BoolVal isJoiningWorld() {
        return new BoolVal(_screen.is(Screens.JOINING));
    }

    public BoolVal isDisconnected() {
        return new BoolVal(_screen.is(Screens.DISCONNECTED));
    }

    public BoolVal onCreditsScreen() {
        return new BoolVal(_screen.is(Screens.CREDITS));
    }

    public BoolVal isPaused() {
        if (_level.isNull())
            return null;
        return new BoolVal(_level.isWorldTickingPaused());
    }

    public BoolVal inGame() {
        return new BoolVal(_level.notNull());
    }


    // ------------------------------------------------------------------------------------------------
    // Time events
    public BoolVal isDay() {
        int time = timeProp.getValue().asInt();                  // "12542" is the time when beds can be used.
        return new BoolVal(time > 23500 || time <= 12500); // "23460" is the time from when beds cannot be used.
    }

    public BoolVal isDawn() {
        int time = timeProp.getValue().asInt();
        return new BoolVal(time > 23500 || time <= 2000);
    }

    public BoolVal isDusk() {
        int time = timeProp.getValue().asInt();
        return new BoolVal(time > 10300 && time <= 12500);
    }

    public BoolVal isNight() {
        int time = timeProp.getValue().asInt();
        return new BoolVal(time > 12500 && time <= 23500);
    }


    // ------------------------------------------------------------------------------------------------
    // Weather events
    public BoolVal isDownfall() {
        if (_level.isNull())
            return null;
        return new BoolVal(_level.isRaining());
    }

    public BoolVal isRaining() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.isRaining() && !_level.isColdEnoughToSnow(_player.blockPos()));
    }

    public BoolVal isSnowing() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.isRaining() && _level.isColdEnoughToSnow(_player.blockPos()));
    }

    public BoolVal isThundering() {
        if (_level.isNull())
            return null;
        return new BoolVal(_level.isThundering());
    }


    // ------------------------------------------------------------------------------------------------
    // Location events
    public BoolVal inVillage() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.countNearbyVillagers(_player.blockPos(), _villageScanHorizontalRadius, _villageScanVerticalRadius) >= _villagerCountThreshold);
    }

    public BoolVal inRanch() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.countNearbyAnimals(_player.blockPos(), _ranchScanHorizontalRadius, _ranchScanVerticalRadius) >= _animalCountThreshold);
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    public BoolVal isDead() {
        return new BoolVal(_screen.is(Screens.DEATH));
    }

    public BoolVal isSleeping() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isSleeping());
    }

    public BoolVal isFishing() {
        if (_player.isNull() || _level.isNull())
            return null;

        // The bopping of the fishing hook creates time periods where the hook is not detected as being in water.
        // To combat this, I make a small grace period of 1000 milliseconds.
        if (_player.fishingHookInWater())
            _latestFishingHookInWaterTime = System.currentTimeMillis();

        if (System.currentTimeMillis() - _latestFishingHookInWaterTime < 1000) {
            _latestFishingPos = _player.position();
            _latestFishingTime = System.currentTimeMillis();
        }
        else if (_latestFishingPos != null) { // Grace period where "isFishing" will not turn off to prevent "shuffling" music back and forth.
            if (_player.distanceTo(_latestFishingPos) > _fishingMoveThreshold || System.currentTimeMillis() - _latestFishingTime > _fishingTimeout)
                _latestFishingPos = null;
        }

        return new BoolVal(_latestFishingPos != null);
    }

    public BoolVal isUnderWater() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isUnderwater());
    }

    public BoolVal inLava() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isInLava());
    }


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    public BoolVal inMinecart() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.inMinecart());
    }

    public BoolVal inBoat() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.inBoat());
    }

    public BoolVal onHorse() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.onHorse());
    }

    public BoolVal onDonkey() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.onDonkey());
    }

    public BoolVal onPig() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.onPig());
    }

    public BoolVal flyingElytra() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.elytraFlying());
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    public BoolVal inCombat() {
        if (_combat.hasActiveCombatants()) {
            _latestCombatTime = System.currentTimeMillis();
            return new BoolVal(true);
        }
        return new BoolVal(System.currentTimeMillis() - _latestCombatTime < _combatGracePeriod);
    }

    public BoolVal inBossFight() {
        return new BoolVal(_combat.inBossFight());
    }



    // ------------------------------------------------------------------------------------------------
    // World properties
    public StringVal getDimensionId() {
        if (_level.isNull())
            return null;
        return new StringVal(_level.getDimensionID());
    }

    public StringVal getBiomeId() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new StringVal(_level.getBiomeID(_player.blockPos()));
    }

    public ListVal getBiomeTagIDs() {
        if (_player.isNull() || _level.isNull())
            return null;
        return ListVal.ofStringList(_level.getBiomeTagIDs(_player.blockPos()));
    }

    public IntVal getTime() {
        if (_level.isNull())
            return null;
        return new IntVal(_level.getTime() % 24000);
    }

    public FloatVal getCaveScore() {
        if (_player.isNull() || _level.isNull())
            return null;
        _latestCaveScore = _caveDetector.getAveragedCaveScore(_level, _player).orElse(_latestCaveScore);
        return new FloatVal((float)(_latestCaveScore));
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    public FloatVal getPlayerHealth() {
        if (_player.isNull())
            return null;
        return new FloatVal(_player.health());
    }

    public FloatVal getPlayerMaxHealth() {
        if (_player.isNull())
            return null;
        return new FloatVal(_player.maxHealth());
    }

    public FloatVal getPlayerElevation() {
        if (_player.isNull())
            return null;
        return new FloatVal((float)_player.vectorY());
    }

    public StringVal getVehicleId() {
        if (_player.isNull())
            return null;
        return new StringVal(_player.vehicleId().orElse(""));
    }

    public ListVal getActiveEffects() {
        if (_player.isNull())
            return null;
        return ListVal.ofStringList(_player.getActiveEffectIds());
    }


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    public IntVal countCombatants() {
        return new IntVal(_combat.countCombatants());
    }

    private StringVal getBoss() {
        return (StringVal)bossesProp.getValue().asList().stream().findFirst().orElse(new StringVal(""));
    }

    public ListVal getBosses() {
        return ListVal.ofStringList(_combat.getBosses());
    }
}
