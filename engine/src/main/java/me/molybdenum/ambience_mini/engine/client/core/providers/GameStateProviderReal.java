package me.molybdenum.ambience_mini.engine.client.core.providers;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.locations.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.locations.StructureCache;
import me.molybdenum.ambience_mini.engine.client.core.caves.CaveDetector;
import me.molybdenum.ambience_mini.engine.client.core.state.*;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.McVersion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GameStateProviderReal<TBlockPos, TVec3, TBlockState, TEntity> extends GameStateProviderTemplate
{
    // State
    private final BasePlayerState<TBlockPos, TVec3, ?> _player;
    private final BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity, ?> _level;
    private final BaseCombatState<TEntity, TVec3> _combat;
    private final CaveDetector<TBlockPos, TVec3, TBlockState> _caveDetector;

    private final BaseScreenState _screen;
    private final ClientAreaManager _areaManager;
    private final StructureCache _structureCache;

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


    @SuppressWarnings("rawtypes")
    public GameStateProviderReal(
        McVersion mcVersion,
        BaseClientCore core,
        BasePlayerState<TBlockPos, TVec3, ?> player,
        BaseLevelState<TBlockPos, TVec3, TBlockState, TEntity, ?> level,
        BaseCombatState<TEntity, TVec3> combatMonitor
    ) {
        super(mcVersion);

        _player = player;
        _level = level;
        _combat = combatMonitor;
        _caveDetector = new CaveDetector<>(core.clientConfig);

        _screen = core.screenState;
        _areaManager = core.areaManager;
        _structureCache = core.structureCache;

        var config = core.clientConfig;
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
            return BoolVal.UNDEFINED;
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
        // "12542" is the time from when beds can be used.
        // "23460" is the time from when beds cannot be used.
        return new BoolVal(
                timeProp.getValue().mapInt(time -> time > 23500 || time <= 12500)
        );
    }

    @Override
    public BoolVal isDawn() {
        return new BoolVal(
                timeProp.getValue().mapInt(time -> time > 23500 || time <= 2000)
        );
    }

    @Override
    public BoolVal isDusk() {
        return new BoolVal(
                timeProp.getValue().mapInt(time -> time > 10300 && time <= 12500)
        );
    }

    @Override
    public BoolVal isNight() {
        return new BoolVal(
                timeProp.getValue().mapInt(time -> time > 12500 && time <= 23500)
        );
    }


    // ------------------------------------------------------------------------------------------------
    // Weather events
    @Override
    public BoolVal isDownfall() {
        return new BoolVal(_level.isRaining());
    }

    @Override
    public BoolVal isRaining() {
        if (_player.isNull() || _level.isNull())
            return BoolVal.UNDEFINED;
        return new BoolVal(_level.isRaining() && !_level.isColdEnoughToSnow(_player.blockPos()));
    }

    @Override
    public BoolVal isSnowing() {
        if (_player.isNull() || _level.isNull())
            return BoolVal.UNDEFINED;
        return new BoolVal(_level.isRaining() && _level.isColdEnoughToSnow(_player.blockPos()));
    }

    @Override
    public BoolVal isThundering() {
        return new BoolVal(_level.isThundering());
    }


    // ------------------------------------------------------------------------------------------------
    // Location events
    @Override
    public BoolVal inVillage() {
        if (_player.isNull() || _level.isNull())
            return BoolVal.UNDEFINED;
        return new BoolVal(_level.countNearbyVillagers(_player.blockPos(), _villageScanHorizontalRadius, _villageScanVerticalRadius) >= _villagerCountThreshold);
    }

    @Override
    public BoolVal inRanch() {
        if (_player.isNull() || _level.isNull())
            return BoolVal.UNDEFINED;
        return new BoolVal(_level.countNearbyAnimals(_player.blockPos(), _ranchScanHorizontalRadius, _ranchScanVerticalRadius) >= _animalCountThreshold);
    }

    @Override
    public BoolVal wardenNearby() {
        if (_player.isNull() || _level.isNull())
            return BoolVal.UNDEFINED;

        Double distance = _level.shortestDistanceToWarden(_player.eyePosition(), Common.WARDEN_SEARCH_RADIUS);
        return distance == null ? BoolVal.FALSE : new BoolVal(distance <= Common.WARDEN_SEARCH_RADIUS);
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state events
    @Override
    public BoolVal isDead() {
        return new BoolVal(_screen.is(Screens.DEATH));
    }

    @Override
    public BoolVal isSleeping() {
        return new BoolVal(_player.isSleeping());
    }

    @Override
    public BoolVal isFishing() {
        if (_player.isNull() || _level.isNull())
            return BoolVal.UNDEFINED;

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
        return new BoolVal(_player.isUnderwater());
    }

    @Override
    public BoolVal inLava() {
        return new BoolVal(_player.isInLava());
    }

    @Override
    public BoolVal isDrowning() {
        return _player.isNull()
                ? BoolVal.UNDEFINED
                : new BoolVal(_player.isDrowning() && _player.isSurvivalOrAdventureMode());
    }


    // ------------------------------------------------------------------------------------------------
    // Mount-like events
    @Override
    public BoolVal inMinecart() {
        return new BoolVal(_player.inMinecart());
    }

    @Override
    public BoolVal inBoat() {
        return new BoolVal(_player.inBoat());
    }

    @Override
    public BoolVal onHorse() {
        return new BoolVal(_player.onHorse());
    }

    @Override
    public BoolVal onDonkey() {
        return new BoolVal(_player.onDonkey());
    }

    @Override
    public BoolVal onPig() {
        return new BoolVal(_player.onPig());
    }

    @Override
    public BoolVal flyingElytra() {
        return new BoolVal(_player.elytraFlying());
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    @Override
    public BoolVal inCombat() {
        if (_level.isNull())
            return BoolVal.UNDEFINED;

        if (_combat.hasActiveCombatants()) {
            _latestCombatTime = System.currentTimeMillis();
            return new BoolVal(true);
        }
        return new BoolVal(System.currentTimeMillis() - _latestCombatTime < _combatGracePeriod);
    }

    @Override
    public BoolVal inBossFight() {
        return _level.isNull()
                ? BoolVal.UNDEFINED
                : new BoolVal(_combat.inBossFight());
    }


    // ------------------------------------------------------------------------------------------------
    // World properties
    @Override
    public StringVal getDifficulty() {
        return _level.isNull()
                ? StringVal.UNDEFINED
                : new StringVal(_level.getDifficulty());
    }

    @Override
    public StringVal getDimensionId() {
        return _level.isNull()
                ? StringVal.UNDEFINED
                : new StringVal(_level.getDimensionID());
    }

    @Override
    public StringVal getBiomeId() {
        return _player.isNull() || _level.isNull()
                ? StringVal.UNDEFINED
                : new StringVal(_level.getBiomeID(_player.blockPos()));
    }

    @Override
    public ListVal getBiomeTagIDs() {
        return _player.isNull() || _level.isNull()
                ? ListVal.UNDEFINED
                : ListVal.ofStringList(_level.getBiomeTagIDs(_player.blockPos()));
    }

    @Override
    public IntVal getTime() {
        return _level.isNull() ? IntVal.UNDEFINED : new IntVal(_level.getTime() % 24000);
    }

    @Override
    public FloatVal getCaveScore() {
        if (_player.isNull() || _level.isNull())
            return FloatVal.UNDEFINED;

        _latestCaveScore = _caveDetector.getAveragedCaveScore(_level, _player).orElse(_latestCaveScore);
        return new FloatVal((float)(_latestCaveScore));
    }

    @Override
    public FloatVal getSkylightScore() {
        if (_player.isNull() || _level.isNull())
            return FloatVal.UNDEFINED;

        List<BlockReading<TBlockPos, TBlockState>> readings = _level.readSurroundings(_player.eyePosition(), 12, 7, 64);
        float averageSkyLight = readings.stream().collect(Collectors.averagingDouble(r -> _level.getAverageSkyLightingAround(r.blockPos()))).floatValue();

        return new FloatVal(averageSkyLight);
    }


    // ------------------------------------------------------------------------------------------------
    // Location properties
    @Override
    public ListVal getIntersectingAreas() {
        if (_player.isNull() || _level.isNull())
            return ListVal.UNDEFINED;

        return new ListVal(
                _areaManager.getIntersectingAreas(
                        _level.getDimensionID(),
                        _level.toAmVector3d(_player.eyePosition())
                ).stream()
                        .sorted(Comparator.comparingDouble(Area::volume))
                        .map(AreaVal::new)
        );
    }

    @Override
    public ListVal getIntersectingStructures() {
        if (_player.isNull() || _level.isNull())
            return ListVal.UNDEFINED;

        //noinspection DataFlowIssue
        return ListVal.ofStringList(
                _structureCache.getIntersectingStructures(_level.getDimensionID(), _level.toAmVector3d(_player.eyePosition()).round())
        );
    }


    // ------------------------------------------------------------------------------------------------
    // Player properties
    @Override
    public StringVal getPlayerUUID() {
        return new StringVal(_player.getUUID());
    }

    @Override
    public StringVal getGameMode() {
        return new StringVal(_player.getGameModeName());
    }

    @Override
    public FloatVal getPlayerHealth() {
        return new FloatVal(_player.health());
    }

    @Override
    public FloatVal getPlayerMaxHealth() {
        return new FloatVal(_player.maxHealth());
    }

    @Override
    public FloatVal getPlayerElevation() {
        return new FloatVal(_player.vectorY().floatValue());
    }

    @Override
    public StringVal getVehicleId() {
        return new StringVal(_player.vehicleId());
    }

    @Override
    public ListVal getActiveEffects() {
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
        return (StringVal)bossesProp.getValue().mapList(
                lst -> lst.stream().findFirst().orElse(new StringVal())
        );
    }

    @Override
    public ListVal getBosses() {
        return ListVal.ofStringList(_combat.getBosses());
    }
}
