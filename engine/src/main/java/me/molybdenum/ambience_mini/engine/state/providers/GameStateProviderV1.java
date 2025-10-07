package me.molybdenum.ambience_mini.engine.state.providers;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.FloatT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.IntT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.StringT;
import me.molybdenum.ambience_mini.engine.BaseConfig;
import me.molybdenum.ambience_mini.engine.state.detectors.BaseCaveDetector;
import me.molybdenum.ambience_mini.engine.state.readers.BaseLevelReader;
import me.molybdenum.ambience_mini.engine.state.readers.PlayerReader;
import me.molybdenum.ambience_mini.engine.state.monitors.BaseScreenMonitor;
import me.molybdenum.ambience_mini.engine.state.monitors.Screens;
import me.molybdenum.ambience_mini.engine.state.readers.VectorCoordinate;

public class GameStateProviderV1<TBlockPos, TVec3, TBlockState> extends BaseGameStateProvider
{
    private final BaseScreenMonitor _screen;
    private final PlayerReader<TBlockPos, TVec3> _player;
    private final BaseLevelReader<TBlockPos, TVec3, TBlockState> _level;
    private final BaseCaveDetector<TBlockPos, TVec3, TBlockState> _caveDetector;

    private final int _villageScanHorizontalRadius;
    private final int _villageScanVerticalRadius;
    private final int _villagerCountThreshold;

    private final int _ranchScanHorizontalRadius;
    private final int _ranchScanVerticalRadius;
    private final int _animalCountThreshold;

    private final int _fishingTimeout;
    private final int _fishingMoveThreshold;

    private VectorCoordinate _latestFishingPos = null;
    private long _latestFishingTime = 0L;

    private double _cashedCaveScore = 0;


    public GameStateProviderV1(
        BaseConfig config,
        BaseScreenMonitor screen,
        PlayerReader<TBlockPos, TVec3> player,
        BaseLevelReader<TBlockPos, TVec3, TBlockState> level,
        BaseCaveDetector<TBlockPos, TVec3, TBlockState> caveDetector
    ) {
        super();

        _screen = screen;
        _player = player;
        _level = level;
        _caveDetector = caveDetector;

        _villageScanHorizontalRadius = config.villageScanHorizontalRadius.get();
        _villageScanVerticalRadius = config.villageScanVerticalRadius.get();
        _villagerCountThreshold = config.villagerCountThreshold.get();

        _ranchScanHorizontalRadius = config.ranchScanHorizontalRadius.get();
        _ranchScanVerticalRadius = config.ranchScanVerticalRadius.get();
        _animalCountThreshold = config.animalCountThreshold.get();

        _fishingTimeout = config.fishingTimeout.get();
        _fishingMoveThreshold = config.fishingMoveThreshold.get();

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
        registerEvent("paused", this::isPaused);
        registerEvent("in_game", this::inGame);
        registerEvent("credits", this::onCreditsScreen);

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
        //registerEvent("in_combat", this::inCombat);  // Will give away if creeper targets player... Perhaps activate only after taking or dealing damage (which requires server-sided setup)?
        registerEvent("boss_fight", this::inBossFight);


        ////
        //// Properties
        ////
        registerProperty("dimension", new StringT(), this::getDimensionId);
        registerProperty("biome", new StringT(), this::getBiomeId);
        registerProperty("time", new IntT(), this::getTime);

        registerProperty("boss", new StringT(), this::getBossId);

        registerProperty("health", new FloatT(), this::getPlayerHealth);
        registerProperty("max_health", new FloatT(), this::getPlayerMaxHealth);
        registerProperty("elevation", new FloatT(), this::getPlayerElevation);
        registerProperty("vehicle", new StringT(), this::getVehicleId);

        registerProperty("cave_score", new FloatT(), this::getCaveScore);


        //TODO: biometag, structure (requires server-sided???) ?
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    public boolean inMainMenu() {
        if (_screen.isScreenNull())
            _screen.memorizedScreen = Screens.NONE;
        return _screen.memorizedScreen == Screens.MAIN_MENU;
    }

    public boolean isJoiningWorld() {
        if (_screen.isScreenNull())
            _screen.memorizedScreen = Screens.NONE;
        return _screen.memorizedScreen == Screens.JOINING;
    }

    public boolean isDisconnected() {
        if (_screen.isScreenNull())
            _screen.memorizedScreen = Screens.NONE;
        return _screen.memorizedScreen == Screens.DISCONNECTED;
    }

    public boolean isPaused() {
        if (_screen.isScreenNull())
            _screen.memorizedScreen = Screens.NONE;
        return _screen.memorizedScreen == Screens.PAUSE;
    }

    public boolean onCreditsScreen() {
        if (_screen.isScreenNull())
            _screen.memorizedScreen = Screens.NONE;
        return _screen.memorizedScreen == Screens.CREDITS;
    }

    public boolean inGame() {
        return _level.notNull();
    }


    // ------------------------------------------------------------------------------------------------
    // Time-based events
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
    // Weather-based events
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
    // Location-based events
    public boolean inVillage() {
        return _player.notNull() && _level.notNull() && _level.countNearbyVillagers(_player.blockPos(), _villageScanHorizontalRadius, _villageScanVerticalRadius) >= _villagerCountThreshold;
    }

    public boolean inRanch() {
        return _player.notNull() && _level.notNull() && _level.countNearbyAnimals(_player.blockPos(), _ranchScanHorizontalRadius, _ranchScanVerticalRadius) >= _animalCountThreshold;
    }

    public boolean isUnderWater() {
        return _player.notNull() && _player.isUnderwater();
    }

    public boolean inLava() {
        return _player.notNull() && _player.isInLava();
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state-based events
    public boolean isDead() {
        return _screen.isDeathScreen();
    }

    public boolean isSleeping() {
        return _player.notNull() && _player.isSleeping();
    }

    public boolean isFishing() {
        if (_player.isNull() || _level.isNull())
            return false;

        if (_player.fishingHookInWater()) {
            _latestFishingPos = _player.vectorCoordinate();
            _latestFishingTime = System.currentTimeMillis();
        }
        else if (_latestFishingPos != null) { // Grace period where "isFishing" will not turn off to prevent "shuffling" music back and forth.
            if (_latestFishingPos.distanceTo(_player.vectorCoordinate()) > _fishingMoveThreshold || System.currentTimeMillis() - _latestFishingTime > _fishingTimeout)
                _latestFishingPos = null;
        }

        return _latestFishingPos != null;
    }


    // ------------------------------------------------------------------------------------------------
    // Mount events
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
    public boolean inBossFight() {
        return _screen.getBossIdIfInFight().isPresent();
    }



    // ------------------------------------------------------------------------------------------------
    // Properties
    public String getDimensionId() {
        return _level.notNull() ? _level.getDimensionId() : "";
    }

    public String getBiomeId() {
        if (_player.isNull() || _level.isNull())
            return "";
        return _level.getBiomeID(_player.blockPos());
    }

    public String getVehicleId() {
        if (_player.isNull())
            return "";
        return _player.vehicleId().orElse("");
    }

    public String getBossId() {
        return _screen.getBossIdIfInFight().orElse("");
    }

    public int getTime() {
        return (_level.notNull() ? _level.getTime() : 0) % 24000;
    }

    public float getPlayerElevation() {
        return (_player.notNull() ? (float)_player.vectorY() : 0);
    }

    public float getPlayerHealth() {
        return (_player.notNull() ? _player.health() : 0);
    }

    public float getPlayerMaxHealth() {
        return (_player.notNull() ? _player.maxHealth() : 0);
    }

    public float getCaveScore() {
        if (_player.isNull() || _level.isNull())
            return 0;
        return (float)(_cashedCaveScore = _caveDetector.getAveragedCaveScore(_level, _player).orElse(_cashedCaveScore));
    }
}
