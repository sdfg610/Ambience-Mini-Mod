package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseCombatState<TEntity, TVec3>
{
    private final ConcurrentHashMap<Integer, Combatant> combatants = new ConcurrentHashMap<>();

    private BasePlayerState<?, TVec3> _playerState;
    private BaseLevelState<?, TVec3, ?, TEntity, ?> _levelState;
    private ServerSetup _serverSetup;

    // Fields for handling leaving combat with no server support
    private int _combatantTimeout;
    private int _leavingCombatDistance;

    private static final long RECHECK_INTERVAL = 1000L;
    private long _latestRecheck = 0L;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core,
            BasePlayerState<?, TVec3> playerState,
            BaseLevelState<?, TVec3, ?, TEntity, ?> levelState
    ) {
        if (_playerState != null)
            throw new RuntimeException("Multiple calls to 'BaseCombatState.init'!");

        _playerState = playerState;
        _levelState = levelState;
        _serverSetup = core.serverSetup;

        _combatantTimeout = core.clientConfig.combatantTimeout.get();
        _leavingCombatDistance = core.clientConfig.leavingCombatDistance.get();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isEntityDead(TEntity entity);
    public abstract int getEntityId(TEntity entity);

    public abstract boolean inBossFight();
    public abstract List<String> getBosses();


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public int countCombatants() {
        // If there is server support, combatants that are targeting the player, but have not interacted with the player
        // yet, are still registered as a combatant -- albeit "inactive". If one combatant becomes active, all potential
        // combatants are counted here so that "horde fight" music can be played as well.
        if (hasActiveCombatants())
            return combatants.size(); // If in creative, there is no combat
        return 0;
    }

    public boolean hasActiveCombatants() {
        if (_playerState.notNull() && _playerState.isSurvivalOrAdventureMode()) {
            recheckCombatants();
            return combatants.values().stream().anyMatch(com -> com.latestInteraction != Long.MIN_VALUE);
        }
        return false;
    }

    private void recheckCombatants() {
        long now = System.currentTimeMillis();
        if (now - _latestRecheck > RECHECK_INTERVAL) {
            // Make new array list of entries to avoid concurrent modification
            for (var entry : new ArrayList<>(combatants.entrySet())) {
                var combatant = entry.getValue();

                var serverlessCheck = _serverSetup.serverVersion.isLessThan(AmVersion.V_2_5_0) && serverlessRemoveCheck(now, combatant);
                if (serverlessCheck || isEntityDead(combatant.entity))
                    combatants.remove(entry.getKey());
            }
            _latestRecheck = now;
        }
    }

    private boolean serverlessRemoveCheck(long now, Combatant combatant) {
        return now - combatant.latestInteraction >= _combatantTimeout
                || _playerState.distanceTo(_levelState.getEntityPosition(combatant.entity)) >= _leavingCombatDistance;
    }


    public void tryAddCombatantByRef(TEntity entity, boolean hasInteraction) {
        addCombatant(getEntityId(entity), entity, hasInteraction);
    }

    public void tryAddCombatantById(int id, boolean hasInteraction) {
        addCombatant(id, null, hasInteraction); // Just pass "null" to avoid a lookup before we know it is necessary
    }

    private void addCombatant(int id, TEntity entity, boolean hasInteraction) {
        if (_playerState.notNull() && _playerState.isSurvivalOrAdventureMode()) {
            var info = combatants.computeIfAbsent(id, ignored ->
                    new Combatant(entity == null ? _levelState.getEntityById(id) : entity) // Lookup if necessary
            );
            if (hasInteraction)
                info.latestInteraction = System.currentTimeMillis();
        }
    }


    public void removeCombatant(int id) {
        combatants.remove(id);
    }

    public void clearCombatants() {
        combatants.clear();
    }


    class Combatant {
        public TEntity entity;
        public long latestInteraction = Long.MIN_VALUE;

        public Combatant(TEntity entity) {
            this.entity = entity;
        }
    }
}
