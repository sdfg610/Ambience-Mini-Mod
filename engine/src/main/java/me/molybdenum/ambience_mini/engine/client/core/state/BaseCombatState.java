package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.CombatantVal;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.ListVal;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseCombatState<TEntity, TVec3>
{
    private final ConcurrentHashMap<Integer, Combatant> combatants = new ConcurrentHashMap<>();

    private BasePlayerState<?, TVec3, ?> _playerState;
    private BaseLevelState<?, TVec3, ?, TEntity, ?> _levelState;
    private ServerSetup _serverSetup;

    // Fields for handling leaving combat with no server support
    private int _combatantTimeout;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core,
            BasePlayerState<?, TVec3, ?> playerState,
            BaseLevelState<?, TVec3, ?, TEntity, ?> levelState
    ) {
        if (_playerState != null)
            throw new RuntimeException("Multiple calls to 'BaseCombatState.init'!");

        _playerState = playerState;
        _levelState = levelState;
        _serverSetup = core.serverSetup;

        _combatantTimeout = core.clientConfig.combatantTimeout.get();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean isEntityDead(TEntity entity);
    public abstract int getEntityId(TEntity entity);

    public abstract String getEntityResourceLocation(TEntity entity);
    public abstract Float getEntityHealth(TEntity entity);
    public abstract Float getEntityMaxHealth(TEntity entity);

    public abstract boolean inBossFight();
    public abstract List<String> getBosses();


    // -----------------------------------------------------------------------------------------------------------------
    // Concrete API
    public int countCombatants() {
        recheckCombatants(System.currentTimeMillis());
        return combatants.size();
    }

    public Boolean isPlayerTargeted() {
        if (_serverSetup.serverVersion.isLessThan(AmVersion.V_2_5_0))
            return null;

        recheckCombatants(System.currentTimeMillis());
        return combatants.values().stream().anyMatch(Combatant::isTargetingPlayer);
    }

    public boolean isPlayerFighting() {
        long now = System.currentTimeMillis();
        recheckCombatants(now);
        return combatants.values().stream().anyMatch(com -> com.isFightingPlayer(now));
    }

    public ListVal getCombatants() {
        long now = System.currentTimeMillis();
        recheckCombatants(now);
        return new ListVal(combatants.values().stream().map(com -> com.asCombatantVal(now)));
    }


    public void handleInteraction(TEntity entity) {
        var combatant = combatants.computeIfAbsent(getEntityId(entity), ignored ->
                new Combatant(entity)
        );

        long now = System.currentTimeMillis();
        combatant.latestInteraction = now;
        recheckCombatants(now);
    }

    public void handleTargeting(int id, boolean targetsPlayer) {
        var combatant = combatants.computeIfAbsent(id, ignored ->
                new Combatant(_levelState.getEntityById(id))
        );

        combatant.isTargetingPlayer = targetsPlayer;
        recheckCombatants(System.currentTimeMillis());
    }


    private void recheckCombatants(long now) {
        // Make new list of entries to avoid concurrent modification (since we are modifying the collection we are iterating over)
        for (var entry : new ArrayList<>(combatants.entrySet())) {
            var combatant = entry.getValue();
            if (combatant.isDead() || !combatant.isTargetingOrFightingPlayer(now))
                combatants.remove(entry.getKey());
        }
    }


    public void removeCombatant(int id) {
        combatants.remove(id);
    }

    public void clearCombatants() {
        combatants.clear();
    }


    class Combatant {
        private final TEntity entity;
        private Boolean isTargetingPlayer = null;
        public long latestInteraction = 0;

        public Combatant(TEntity entity) {
            this.entity = entity;
        }

        public CombatantVal asCombatantVal(long now) {
            return new CombatantVal(
                    entity == null ? null : getEntityResourceLocation(entity),
                    entity == null ? null : getEntityHealth(entity),
                    entity == null ? null : getEntityMaxHealth(entity),
                    isTargetingPlayer,
                    isFightingPlayer(now)
            );
        }


        public Boolean isTargetingPlayer() {
            return isTargetingPlayer != null && isTargetingPlayer;
        }

        public boolean isFightingPlayer(long now) {
            return now - latestInteraction <= _combatantTimeout;
        }

        public boolean isTargetingOrFightingPlayer(long now) {
            return isTargetingPlayer() || isFightingPlayer(now);
        }

        public boolean isDead() {
            return entity == null || isEntityDead(entity);
        }
    }
}
