package me.molybdenum.ambience_mini.engine.core.providers;

import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.core.detectors.CaveDetector;
import me.molybdenum.ambience_mini.engine.core.state.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameStateProviderV1Real<TBlockPos, TVec3, TBlockState, TEntity> extends GameStateProviderV1Template
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
    private final Property timeProp;
    private final Property bossesProp;


    public GameStateProviderV1Real(
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

        timeProp = getProperty("time");
        bossesProp = getProperty("bosses");
    }


    // ------------------------------------------------------------------------------------------------
    // Execution
    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        _player.prepare(messages);
        _level.prepare(messages);
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    @Override
    public BoolVal inMainMenu() {
        return new BoolVal(_screen.is(Screens.MAIN_MENU));
    }

    @Override
    public BoolVal isJoiningWorld() {
        return new BoolVal(_screen.is(Screens.JOINING));
    }

    @Override
    public BoolVal isDisconnected() {
        return new BoolVal(_screen.is(Screens.DISCONNECTED));
    }

    @Override
    public BoolVal onCreditsScreen() {
        return new BoolVal(_screen.is(Screens.CREDITS));
    }

    @Override
    public BoolVal isPaused() {
        if (_level.isNull())
            return null;
        return new BoolVal(_level.isWorldTickingPaused());
    }

    @Override
    public BoolVal inGame() {
        return new BoolVal(_level.notNull());
    }


    // ------------------------------------------------------------------------------------------------
    // Time events
    @Override
    public BoolVal isDay() {
        int time = timeProp.getValue().asInt();                  // "12542" is the time when beds can be used.
        return new BoolVal(time > 23500 || time <= 12500); // "23460" is the time from when beds cannot be used.
    }

    @Override
    public BoolVal isDawn() {
        int time = timeProp.getValue().asInt();
        return new BoolVal(time > 23500 || time <= 2000);
    }

    @Override
    public BoolVal isDusk() {
        int time = timeProp.getValue().asInt();
        return new BoolVal(time > 10300 && time <= 12500);
    }

    @Override
    public BoolVal isNight() {
        int time = timeProp.getValue().asInt();
        return new BoolVal(time > 12500 && time <= 23500);
    }


    // ------------------------------------------------------------------------------------------------
    // Weather events
    @Override
    public BoolVal isDownfall() {
        if (_level.isNull())
            return null;
        return new BoolVal(_level.isRaining());
    }

    @Override
    public BoolVal isRaining() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.isRaining() && !_level.isColdEnoughToSnow(_player.blockPos()));
    }

    @Override
    public BoolVal isSnowing() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.isRaining() && _level.isColdEnoughToSnow(_player.blockPos()));
    }

    @Override
    public BoolVal isThundering() {
        if (_level.isNull())
            return null;
        return new BoolVal(_level.isThundering());
    }


    // ------------------------------------------------------------------------------------------------
    // Location events
    @Override
    public BoolVal inVillage() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.countNearbyVillagers(_player.blockPos(), _villageScanHorizontalRadius, _villageScanVerticalRadius) >= _villagerCountThreshold);
    }

    @Override
    public BoolVal inRanch() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new BoolVal(_level.countNearbyAnimals(_player.blockPos(), _ranchScanHorizontalRadius, _ranchScanVerticalRadius) >= _animalCountThreshold);
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    @Override
    public BoolVal isDead() {
        return new BoolVal(_screen.is(Screens.DEATH));
    }

    @Override
    public BoolVal isSleeping() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isSleeping());
    }

    @Override
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

    @Override
    public BoolVal isUnderWater() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isUnderwater());
    }

    @Override
    public BoolVal inLava() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isInLava());
    }

    @Override
    public BoolVal isDrowning() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.isDrowning() && _player.isSurvivalOrAdventureMode());
    }


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    @Override
    public BoolVal inMinecart() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.inMinecart());
    }

    @Override
    public BoolVal inBoat() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.inBoat());
    }

    @Override
    public BoolVal onHorse() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.onHorse());
    }

    @Override
    public BoolVal onDonkey() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.onDonkey());
    }

    @Override
    public BoolVal onPig() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.onPig());
    }

    @Override
    public BoolVal flyingElytra() {
        if (_player.isNull())
            return null;
        return new BoolVal(_player.elytraFlying());
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    @Override
    public BoolVal inCombat() {
        if (_combat.hasActiveCombatants()) {
            _latestCombatTime = System.currentTimeMillis();
            return new BoolVal(true);
        }
        return new BoolVal(System.currentTimeMillis() - _latestCombatTime < _combatGracePeriod);
    }

    @Override
    public BoolVal inBossFight() {
        return new BoolVal(_combat.inBossFight());
    }


    // ------------------------------------------------------------------------------------------------
    // World properties
    @Override
    public StringVal getDifficulty() {
        if (_level.isNull())
            return new StringVal("");
        return new StringVal(_level.getDifficulty());
    }

    @Override
    public StringVal getDimensionId() {
        if (_level.isNull())
            return null;
        return new StringVal(_level.getDimensionID());
    }

    @Override
    public StringVal getBiomeId() {
        if (_player.isNull() || _level.isNull())
            return null;
        return new StringVal(_level.getBiomeID(_player.blockPos()));
    }

    @Override
    public ListVal getBiomeTagIDs() {
        if (_player.isNull() || _level.isNull())
            return null;
        return ListVal.ofStringList(_level.getBiomeTagIDs(_player.blockPos()));
    }

    @Override
    public IntVal getTime() {
        if (_level.isNull())
            return null;
        return new IntVal(_level.getTime() % 24000);
    }

    @Override
    public FloatVal getCaveScore() {
        if (_player.isNull() || _level.isNull())
            return null;
        _latestCaveScore = _caveDetector.getAveragedCaveScore(_level, _player).orElse(_latestCaveScore);
        return new FloatVal((float)(_latestCaveScore));
    }

    @Override
    public FloatVal getSkylightScore() {
        if (_player.isNull() || _level.isNull())
            return null;

        List<BlockReading<TBlockPos, TBlockState>> readings = _level.readSurroundings(_player.eyePosition(), 12, 5, 32);
        float averageSkyLight = readings.stream().collect(Collectors.averagingDouble(r -> _level.getAverageSkyLightingAround(r.blockPos()))).floatValue();

        return new FloatVal(averageSkyLight);
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    @Override
    public StringVal getGameMode() {
        if (_player.isNull())
            return new StringVal("");
        return new StringVal(_player.getGameMode());
    }

    @Override
    public FloatVal getPlayerHealth() {
        if (_player.isNull())
            return null;
        return new FloatVal(_player.health());
    }

    @Override
    public FloatVal getPlayerMaxHealth() {
        if (_player.isNull())
            return null;
        return new FloatVal(_player.maxHealth());
    }

    @Override
    public FloatVal getPlayerElevation() {
        if (_player.isNull())
            return null;
        return new FloatVal((float)_player.vectorY());
    }

    @Override
    public StringVal getVehicleId() {
        if (_player.isNull())
            return null;
        return new StringVal(_player.vehicleId().orElse(""));
    }

    @Override
    public ListVal getActiveEffects() {
        if (_player.isNull())
            return null;
        return ListVal.ofStringList(_player.getActiveEffectIds());
    }


    // ------------------------------------------------------------------------------------------------
    // Combat properties
    @Override
    public IntVal countCombatants() {
        return new IntVal(_combat.countCombatants());
    }

    @Override
    public StringVal getBoss() {
        return (StringVal)bossesProp.getValue().asList().stream().findFirst().orElse(new StringVal(""));
    }

    @Override
    public ListVal getBosses() {
        return ListVal.ofStringList(_combat.getBosses());
    }
}
