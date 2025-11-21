package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.core.state.BaseCombatState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class CombatState extends BaseCombatState<Entity, Vec3>
{
    public CombatState(
            BaseClientConfig config,
            PlayerState playerReader,
            LevelState levelReader,
            ServerSetup serverSetup
    ) {
        super(config, playerReader, levelReader, serverSetup);
    }

    @Override
    public boolean isEntityDead(Entity entity) {
        return !entity.isAlive();
    }

    @Override
    public int getEntityId(Entity entity) {
        return 0;
    }
}
