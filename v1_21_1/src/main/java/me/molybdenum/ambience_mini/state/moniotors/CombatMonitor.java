package me.molybdenum.ambience_mini.state.moniotors;

import me.molybdenum.ambience_mini.engine.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.state.monitors.BaseCombatMonitor;
import me.molybdenum.ambience_mini.state.readers.LevelReader_1_21;
import me.molybdenum.ambience_mini.state.readers.PlayerReader_1_21;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class CombatMonitor extends BaseCombatMonitor<Entity, Vec3>
{
    public CombatMonitor(
            BaseClientConfig config,
            PlayerReader_1_21 playerReader,
            LevelReader_1_21 levelReader) {
        super(config, playerReader, levelReader);
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
