package me.molybdenum.ambience_mini.engine.core.state;

import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.core.setup.ServerSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseCombatState<TEntity, TVec3>
{
    private final ConcurrentHashMap<Integer, Combatant> combatants = new ConcurrentHashMap<>();

    private final BasePlayerState<?, TVec3> _playerState;
    private final BaseLevelState<?, TVec3, ?, TEntity> _levelState;
    private final ServerSetup _serverSetup;

    // Fields for handling leaving combat with no server support
    protected final int _combatantTimeout;
    protected final int _leavingCombatDistance;

    private long _latestRecheck = 0L;


    protected BaseCombatState(
            BaseClientConfig config,
            BasePlayerState<?, TVec3> playerReader,
            BaseLevelState<?, TVec3, ?, TEntity> levelReader,
            ServerSetup serverSetup
    ) {
        _playerState = playerReader;
        _levelState = levelReader;
        _serverSetup = serverSetup;

        _combatantTimeout = config.combatantTimeout.get();
        _leavingCombatDistance = config.leavingCombatDistance.get();
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
        if (!_playerState.isSurvivalOrAdventureMode())
            return false; // If in creative, there is no combat

        recheckCombatants();
        return combatants.values().stream().anyMatch(com -> com.latestInteraction != Long.MIN_VALUE);
    }

    private void recheckCombatants() {
        long now = System.currentTimeMillis();
        if (now - _latestRecheck > 1000) {
            // Make new array list of entries to avoid concurrent modification
            for (var entry : new ArrayList<>(combatants.entrySet())) {
                var combatant = entry.getValue();

                var serverlessCheck = !_serverSetup.hasServerSupport && serverlessRemoveCheck(now, combatant);
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
        addCombatant(id, null, hasInteraction); // null -- Don't do a lookup before we know it is necessary
    }

    private void addCombatant(int id, TEntity entity, boolean hasInteraction) {
        if (_playerState.isSurvivalOrAdventureMode()) { // If in creative, there is no combat
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
