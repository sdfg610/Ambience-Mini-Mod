package me.molybdenum.ambience_mini.engine.state.providers;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.FloatT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.IntT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.ListT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.StringT;
import me.molybdenum.ambience_mini.engine.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.state.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.state.monitors.BaseCombatMonitor;
import me.molybdenum.ambience_mini.engine.state.monitors.Screens;
import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.PlayerReader;
import me.molybdenum.ambience_mini.engine.state.monitors.BaseScreenMonitor;

import java.util.List;

public class GameStateProviderV1<TBlockPos, TVec3, TBlockState, TEntity> extends BaseGameStateProvider
{
    private final PlayerReader<TBlockPos, TVec3> _player;
    private final BaseLevelReader<TBlockPos, TVec3, TBlockState, TEntity> _level;

    private final BaseScreenMonitor _screenMonitor;
    private final BaseCombatMonitor<TEntity, TVec3> _combatMonitor;
    private final CaveDetector<TBlockPos, TVec3, TBlockState> _caveDetector;

    private final int _villageScanHorizontalRadius;
    private final int _villageScanVerticalRadius;
    private final int _villagerCountThreshold;

    private final int _ranchScanHorizontalRadius;
    private final int _ranchScanVerticalRadius;
    private final int _animalCountThreshold;

    private final int _fishingTimeout;
    private final int _fishingMoveThreshold;
    private TVec3 _latestFishingPos = null;
    private long _latestFishingTime = 0L;
    private long _latestFishingHookInWaterTime = 0L;

    private final int _combatGracePeriod;
    private long _latestCombatTime = 0L;

    private double _cashedCaveScore = 0;


    public GameStateProviderV1(
        BaseClientConfig config,
        PlayerReader<TBlockPos, TVec3> player,
        BaseLevelReader<TBlockPos, TVec3, TBlockState, TEntity> level,
        BaseScreenMonitor screenMonitor,
        BaseCombatMonitor<TEntity, TVec3> combatMonitor,
        CaveDetector<TBlockPos, TVec3, TBlockState> caveDetector
    ) {
        super();

        _player = player;
        _level = level;

        _screenMonitor = screenMonitor;
        _combatMonitor = combatMonitor;
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
        registerProperty("time", new IntT(), this::getTime);
        registerProperty("cave_score", new FloatT(), this::getCaveScore);

        // Player properties
        registerProperty("health", new FloatT(), this::getPlayerHealth);
        registerProperty("max_health", new FloatT(), this::getPlayerMaxHealth);
        registerProperty("elevation", new FloatT(), this::getPlayerElevation);
        registerProperty("vehicle", new StringT(), this::getVehicleId);
        registerProperty("effects", new ListT(new StringT()), this::getActiveEffects);

        // Combat properties
        registerProperty("combatant_count", new IntT(), this::countCombatants);
        registerProperty("boss", new StringT(), () -> getBosses().stream().findFirst().orElse(""));
        registerProperty("bosses", new ListT(new StringT()), this::getBosses);


        //TODO: Structure detection maybe?
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    public boolean inMainMenu() {
        return _screenMonitor.is(Screens.MAIN_MENU);
    }

    public boolean isJoiningWorld() {
        return _screenMonitor.is(Screens.JOINING);
    }

    public boolean isDisconnected() {
        return _screenMonitor.is(Screens.DISCONNECTED);
    }

    public boolean onCreditsScreen() {
        return _screenMonitor.is(Screens.CREDITS);
    }

    public boolean isPaused() {
        return _level.isWorldTickingPaused();
    }

    public boolean inGame() {
        return _level.notNull();
    }


    // ------------------------------------------------------------------------------------------------
    // Time events
    public boolean isDay() {
        int time = this.getTime();            // "12542" is the time when beds can be used.
        return time > 23500 || time <= 12500; // "23460" is the time from when beds cannot be used.
    }

    public boolean isDawn() {
        int time = this.getTime();
        return time > 23500 || time <= 2000;
    }

    public boolean isDusk() {
        int time = this.getTime();
        return time > 10300 && time <= 12500;
    }

    public boolean isNight() {
        int time = this.getTime();
        return time > 12500 && time <= 23500;
    }


    // ------------------------------------------------------------------------------------------------
    // Weather events
    public boolean isDownfall() {
        return _level.notNull() && _level.isRaining();
    }

    public boolean isRaining() {
        return _player.notNull() && _level.notNull() && _level.isRaining() && !_level.isColdEnoughToSnow(_player.blockPos());
    }

    public boolean isSnowing() {
        return _player.notNull() && _level.notNull() && _level.isRaining() && _level.isColdEnoughToSnow(_player.blockPos());
    }

    public boolean isThundering() {
        return _level.notNull() && _level.isThundering();
    }


    // ------------------------------------------------------------------------------------------------
    // Location events
    public boolean inVillage() {
        return _player.notNull() && _level.notNull() && _level.countNearbyVillagers(_player.blockPos(), _villageScanHorizontalRadius, _villageScanVerticalRadius) >= _villagerCountThreshold;
    }

    public boolean inRanch() {
        return _player.notNull() && _level.notNull() && _level.countNearbyAnimals(_player.blockPos(), _ranchScanHorizontalRadius, _ranchScanVerticalRadius) >= _animalCountThreshold;
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    public boolean isDead() {
        return _screenMonitor.is(Screens.DEATH);
    }

    public boolean isSleeping() {
        return _player.notNull() && _player.isSleeping();
    }

    public boolean isFishing() {
        if (_player.isNull() || _level.isNull())
            return false;

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

        return _latestFishingPos != null;
    }

    public boolean isUnderWater() {
        return _player.notNull() && _player.isUnderwater();
    }

    public boolean inLava() {
        return _player.notNull() && _player.isInLava();
    }


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    public boolean inMinecart() {
        return _player.notNull() && _player.inMinecart();
    }

    public boolean inBoat() {
        return _player.notNull() && _player.inBoat();
    }

    public boolean onHorse() {
        return _player.notNull() && _player.onHorse();
    }

    public boolean onDonkey() {
        return _player.notNull() && _player.onDonkey();
    }

    public boolean onPig() {
        return _player.notNull() && _player.onPig();
    }

    public boolean flyingElytra() {
        return _player.notNull() && _player.elytraFlying();
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    public boolean inCombat() {
        if (_combatMonitor.hasActiveCombatants()) {
            _latestCombatTime = System.currentTimeMillis();
            return true;
        }
        return System.currentTimeMillis() - _latestCombatTime < _combatGracePeriod;
    }

    public boolean inBossFight() {
        return _player.isInBossFight();
    }



    // ------------------------------------------------------------------------------------------------
    // World properties
    public String getDimensionId() {
        return _level.notNull() ? _level.getDimensionID() : "";
    }

    public String getBiomeId() {
        if (_player.isNull() || _level.isNull())
            return "";
        return _level.getBiomeID(_player.blockPos());
    }

    public List<String> getBiomeTagIDs() {
        if (_player.isNull() || _level.isNull())
            return List.of();
        return _level.getBiomeTagIDs(_player.blockPos());
    }

    public int getTime() {
        return (_level.notNull() ? _level.getTime() : 0) % 24000;
    }

    public float getCaveScore() {
        if (_player.isNull() || _level.isNull())
            return 0;
        return (float)(_cashedCaveScore = _caveDetector.getAveragedCaveScore(_level, _player).orElse(_cashedCaveScore));
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    public float getPlayerHealth() {
        return (_player.notNull() ? _player.health() : 0);
    }

    public float getPlayerMaxHealth() {
        return (_player.notNull() ? _player.maxHealth() : 0);
    }

    public float getPlayerElevation() {
        return (_player.notNull() ? (float)_player.vectorY() : 0);
    }

    public String getVehicleId() {
        if (_player.isNull())
            return "";
        return _player.vehicleId().orElse("");
    }

    public List<String> getActiveEffects() {
        if (_player.isNull())
            return List.of();
        return _player.getActiveEffectIds();
    }


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    public int countCombatants() {
        return _combatMonitor.countCombatants();
    }

    public List<String> getBosses() {
        if (_player.isNull())
            return List.of();
        return _player.getBosses();
    }
}
